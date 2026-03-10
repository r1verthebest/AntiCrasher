package core.sunshine.anticrash.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ActionbarUtil {

	private static final ProtocolManager PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager();
	private static final Logger LOGGER = Logger.getLogger("SunshineShield");

	private ActionbarUtil() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static void sendActionbar(Player player, String message) {
		if (player == null || !player.isOnline())
			return;

		PacketContainer chatPacket = PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.CHAT);

		chatPacket.getChatComponents().write(0, WrappedChatComponent.fromText(message));
		chatPacket.getBytes().write(0, (byte) 2); // ID 2 = Action Bar

		try {
			PROTOCOL_MANAGER.sendServerPacket(player, chatPacket);
		} catch (InvocationTargetException e) {
			LOGGER.log(Level.SEVERE, "Erro ao enviar pacote de Actionbar para " + player.getName(), e);
		}
	}
}