package core.sunshine.impl;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import core.sunshine.anticrash.AntiCrash;
import core.sunshine.checks.AbstractCheck;
import core.sunshine.config.ConfigCache;
import net.minecraft.server.v1_8_R3.BlockPosition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class BlockPlaceCheck extends AbstractCheck {

	public BlockPlaceCheck() {
		super("BlockPlaceCheck", Client.BLOCK_PLACE);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		PacketContainer packet = event.getPacket();
		Player player = event.getPlayer();

		ItemStack stack = packet.getItemModifier().readSafely(0);
		if (stack == null) {
			sendCrashWarning(player, event, "Tentativa de colocar item nulo.");
			return;
		}

		BlockPosition pos = packet.getSpecificModifier(BlockPosition.class).readSafely(0);
		if (pos == null)
			return;

		if (player.getWorld() == null)
			return;

		double px = player.getLocation().getX();
		double py = player.getLocation().getY();
		double pz = player.getLocation().getZ();

		double bx = pos.getX();
		double by = pos.getY();
		double bz = pos.getZ();

		if (Math.abs(bx) < 16.0 && Math.abs(bz) < 16.0)
			return;

		double distSq = Math.pow(px - bx, 2) + Math.pow(py - by, 2) + Math.pow(pz - bz, 2);

		double maxDist = ConfigCache.getInstance().getValue("placecheck.maxDistance", 32, Integer.class);
		double maxDistSq = maxDist * maxDist;

		if (distSq > maxDistSq) {
			int minTps = ConfigCache.getInstance().getValue("placecheck.maxTps", 15, Integer.class);

			if (AntiCrash.getInstance().getTpsCalculator().getCurrentTps() > minTps) {
				sendCrashWarning(player, event, "Posição de bloco muito distante (> " + maxDist + " blocos)");
			}
		}
	}
}