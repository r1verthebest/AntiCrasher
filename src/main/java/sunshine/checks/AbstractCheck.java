package core.sunshine.checks;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import core.sunshine.Core;
import core.sunshine.anticrash.player.PlayerRegister;
import core.sunshine.anticrash.utils.NMSUtils;
import core.sunshine.config.ConfigCache;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class AbstractCheck extends PacketAdapter implements ICheck {

	private static final Map<UUID, String> LAST_REASONS = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> LAST_MESSAGE_TIME = new ConcurrentHashMap<>();

	private final String name;

	public AbstractCheck(String name, PacketType... types) {
		super(Core.getPlugin(), types);
		this.name = name;
		if (isEnable()) {
			ProtocolLibrary.getProtocolManager().addPacketListener(this);
		}
	}

	public boolean isEnable() {
		return ConfigCache.getInstance().isCheckEnable(this.name);
	}

	public void sendCrashWarning(Player player, PacketEvent event, String reason) {
		if (event != null)
			event.setCancelled(true);

		UUID uuid = player.getUniqueId();
		long now = System.currentTimeMillis();

		if (LAST_REASONS.containsKey(uuid) && LAST_REASONS.get(uuid).equals(reason)) {
			if (LAST_MESSAGE_TIME.getOrDefault(uuid, 0L) + 500L > now)
				return;
		}

		LAST_REASONS.put(uuid, reason);
		LAST_MESSAGE_TIME.put(uuid, now);

		String packetName = (event != null) ? event.getPacket().getType().name() : "N/A";
		String displayMessage = "§c§lSHIELD §fSuspeito: §c" + player.getName() + " §f- Crash via §7" + packetName;

		new Broadcast("anticrash.notify", (staff) -> {
			staff.sendMessage(displayMessage);
			staff.sendMessage("§c§lSHIELD §fMotivo: §7" + reason);
		});

		Bukkit.getScheduler().runTask(getPlugin(), () -> {
			closeChannel(player);
			player.kickPlayer("§c§lSUNSHINE SHIELD\n\n§fConexão encerrada por comportamento anômalo.");
		});
	}

	public void closeChannel(Player player) {
		try {
			Channel channel = NMSUtils.getChannel(player);
			if (channel != null && channel.isOpen()) {
				channel.close();
			}
		} catch (Exception ignored) {
		}
	}

	@Override
	public void registerFACPlayer(PlayerRegister player) {
	}

	@Override
	public String getName() {
		return this.name;
	}

	public static class Broadcast {
		public Broadcast(String permission, Consumer<CommandSender> action) {
			Bukkit.getOnlinePlayers().stream().filter(p -> permission == null || p.hasPermission(permission))
					.forEach(action);

			action.accept(Bukkit.getConsoleSender());
		}
	}
}