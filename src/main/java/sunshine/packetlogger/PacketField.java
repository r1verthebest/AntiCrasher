package core.sunshine.packetlogger;

public class PacketField {

	private final String type;
	private final Object value;

	public PacketField(String type, Object value) {
		this.type = type != null ? type : "Unknown";
		this.value = value;
	}

	@Override
	public String toString() {
		String displayValue = (value == null) ? "null" : value.toString();
		return String.format("[%s] %s", type, displayValue);
	}

	public String getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}

	public String getValueAsString() {
		return String.valueOf(value);
	}
}