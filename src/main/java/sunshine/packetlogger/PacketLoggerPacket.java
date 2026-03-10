package core.sunshine.packetlogger;

import net.minecraft.server.v1_8_R3.Packet;
import java.util.LinkedHashMap;
import java.util.Map;

public class PacketLoggerPacket {

	private final Map<String, PacketField> fields = new LinkedHashMap<>();
	private final Packet<?> packet;

	public PacketLoggerPacket(Packet<?> packet) {
		this.packet = packet;
	}

	public Packet<?> getPacket() {
		return this.packet;
	}

	public Map<String, PacketField> getFields() {
		return this.fields;
	}

	public String getPacketName() {
		return packet != null ? packet.getClass().getSimpleName() : "UnknownPacket";
	}
}