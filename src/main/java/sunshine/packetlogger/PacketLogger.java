package core.sunshine.packetlogger;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying.PacketPlayInLook;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying.PacketPlayInPosition;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying.PacketPlayInPositionLook;

import org.bukkit.entity.Player;

import core.sunshine.Core;
import core.sunshine.nettyinjections.PacketInjection;

public class PacketLogger implements PacketInjection.PacketListener {

	private final File dir = new File(Core.getPlugin().getDataFolder(), "packetLogs");

	private static final Set<Class<?>> IGNORE_PACKETS = new HashSet<>(Arrays.asList(PacketPlayInFlying.class,
			PacketPlayInPosition.class, PacketPlayInKeepAlive.class, PacketPlayInArmAnimation.class,
			PacketPlayInLook.class, PacketPlayInPositionLook.class, PacketPlayInTransaction.class));

	private final Player player;
	private final PacketLoggerFileWriter fileWriter;
	private final PacketInjection packetInjection;
	private volatile boolean logging = false;

	public PacketLogger(Player player, PacketInjection packetInjection) {
		this.player = player;
		this.packetInjection = packetInjection;

		if (!this.dir.exists()) {
			this.dir.mkdirs();
		}

		this.fileWriter = new PacketLoggerFileWriter(this);
		this.packetInjection.addPacketListener(this);
	}

	@Override
	public boolean handlePacket(Player player, Packet<?> packet) {
		if (!logging || packet == null)
			return true;

		Class<?> packetClass = packet.getClass();
		if (IGNORE_PACKETS.contains(packetClass)) {
			return true;
		}

		PacketLoggerPacket logPacket = new PacketLoggerPacket(packet);
		Class<?> currentClass = packetClass;
		while (currentClass != null && currentClass != Object.class) {
			for (Field field : currentClass.getDeclaredFields()) {
				try {
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}

					String typeName = field.getType().isPrimitive() ? field.getType().getName()
							: field.getType().getSimpleName();

					Object value = field.get(packet);

					if (field.getType().isArray() && value != null) {
						if (value instanceof Object[]) {
							value = Arrays.deepToString((Object[]) value);
						} else {
							value = "PrimitiveArray[" + java.lang.reflect.Array.getLength(value) + "]";
						}
					}

					logPacket.getFields().put(field.getName(), new PacketField(typeName, value));

				} catch (Exception e) {
				}
			}
			currentClass = currentClass.getSuperclass();
		}

		this.fileWriter.writePacket(logPacket);
		return true;
	}

	public void startLogging() {
		this.logging = true;
		this.fileWriter.start();
	}

	public void stopLogging() {
		this.logging = false;
		this.fileWriter.stop();
	}

	public File getDIR() {
		return this.dir;
	}

	public Player getPlayer() {
		return this.player;
	}

	public boolean isLogging() {
		return this.logging;
	}
}