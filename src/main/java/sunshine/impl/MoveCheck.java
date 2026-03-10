package core.sunshine.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import core.sunshine.Core;
import core.sunshine.checks.AbstractCheck;
import core.sunshine.config.ConfigCache;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MoveCheck extends AbstractCheck implements Listener {

	private final Map<UUID, Location> lastLocations = new ConcurrentHashMap<>();
	private final Map<UUID, Integer> packetsSent = new ConcurrentHashMap<>();

	public MoveCheck() {
		super("MoveCheck", PacketType.Play.Client.POSITION);
		Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
		Bukkit.getScheduler().runTaskTimerAsynchronously(Core.getPlugin(), packetsSent::clear, 20L, 20L);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		Player player = event.getPlayer();
		if (player == null)
			return;
		UUID uuid = player.getUniqueId();

		double x = event.getPacket().getDoubles().read(0);
		double y = event.getPacket().getDoubles().read(1);
		double z = event.getPacket().getDoubles().read(2);

		if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z) || Math.abs(x) > 30000000
				|| Math.abs(y) > 30000000) {
			event.setCancelled(true);
			sendCrashWarning(player, event, "Coordenadas inválidas ou fora do limite do mundo.");
			return;
		}

		int count = packetsSent.getOrDefault(uuid, 0) + 1;
		packetsSent.put(uuid, count);
		if (count > 60) {
			event.setCancelled(true);
			if (count == 61)
				sendCrashWarning(player, event, "Excesso de pacotes de posição (>60 PPS).");
			return;
		}

		Location lastLoc = lastLocations.get(uuid);
		if (lastLoc != null) {
			double maxDist = ConfigCache.getInstance().getValue("moveCheck.positionPacketDistance", 100, Integer.class);
			double distSq = Math.pow(x - lastLoc.getX(), 2) + Math.pow(y - lastLoc.getY(), 2)
					+ Math.pow(z - lastLoc.getZ(), 2);

			if (distSq > Math.pow(maxDist, 2)) {
				event.setCancelled(true);
				sendCrashWarning(player, event, "Teleporte via pacote excedeu distância máxima segura.");
				return;
			}
		}

		lastLocations.put(uuid, new Location(player.getWorld(), x, y, z));
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onMove(PlayerMoveEvent event) {
		if (!event.getTo().getWorld().isChunkLoaded(event.getTo().getBlockX() >> 4, event.getTo().getBlockZ() >> 4)) {
			event.setCancelled(true);
			return;
		}

		double distSq = event.getFrom().distanceSquared(event.getTo());
		double maxFlag = Math.pow(ConfigCache.getInstance().getValue("moveCheck.flagDistance", 10, Integer.class), 2);

		if (distSq > maxFlag) {
			event.setCancelled(true);
			sendCrashWarning(event.getPlayer(), null,
					"Movimentação impossível detectada (> " + Math.sqrt(maxFlag) + " blocks/tick)");
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		lastLocations.remove(uuid);
		packetsSent.remove(uuid);
	}
}