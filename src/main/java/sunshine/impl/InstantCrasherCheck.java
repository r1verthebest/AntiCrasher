package core.sunshine.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import core.sunshine.checks.AbstractCheck;
import org.bukkit.entity.Player;

public final class InstantCrasherCheck extends AbstractCheck {

	public InstantCrasherCheck() {
		super("InstantCrasherCheck", PacketType.Handshake.Client.SET_PROTOCOL);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		Player player = event.getPlayer();
		if (player == null)
			return;

		try {
			if (player.getAddress() == null || player.getAddress().getAddress() == null) {
				event.setCancelled(true);
				sendCrashWarning(player, event, "Conexão sem endereço IP válido (Handshake Exploit)");
			}
		} catch (Exception e) {
			event.setCancelled(true);
		}
	}
}