package core.sunshine.anticrash.utils;

import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NMSUtils {

	private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	private static final Logger LOGGER = Logger.getLogger("SunshineShield");

	private static Method getHandleMethod;
	private static Field playerConnectionField;
	private static Field networkManagerField;
	private static Field channelField;

	static {
		try {
			Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + VERSION + ".entity.CraftPlayer");
			getHandleMethod = craftPlayerClass.getDeclaredMethod("getHandle");

			Class<?> entityPlayerClass = getNMSClass("EntityPlayer");
			playerConnectionField = entityPlayerClass.getDeclaredField("playerConnection");

			Class<?> playerConnectionClass = getNMSClass("PlayerConnection");
			networkManagerField = playerConnectionClass.getDeclaredField("networkManager");

			Class<?> networkManagerClass = getNMSClass("NetworkManager");
			channelField = networkManagerClass.getDeclaredField("channel");

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Falha crítica ao inicializar reflexões NMS para a versão " + VERSION, e);
		}
	}

	private NMSUtils() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server." + VERSION + "." + nmsClassString);
	}

	public static Object getConnection(Player player) {
		try {
			Object nmsPlayer = getHandleMethod.invoke(player);
			return playerConnectionField.get(nmsPlayer);
		} catch (Exception e) {
			return null;
		}
	}

	public static Channel getChannel(Player player) {
		try {
			Object connection = getConnection(player);
			Object networkManager = networkManagerField.get(connection);
			return (Channel) channelField.get(networkManager);
		} catch (Exception e) {
			return null;
		}
	}
}