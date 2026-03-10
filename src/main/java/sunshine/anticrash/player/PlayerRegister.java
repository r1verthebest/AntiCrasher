package core.sunshine.anticrash.player;

import core.sunshine.anticrash.AntiCrash;
import core.sunshine.nettyinjections.NettyDecodeInjection;
import core.sunshine.nettyinjections.PacketInjection;
import core.sunshine.packetlogger.PacketLogger;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PlayerRegister {

	private final long joinedAt;
	private final UUID uuid;
	private final String ip;

	private long lastOnlineAt;
	private boolean online;

	private final PacketInjection packetInjection;
	private final NettyDecodeInjection nettyDecodeInjection;
	private final PacketLogger packetLogger;

	public PlayerRegister(long joinedAt, Player player) {
		this.joinedAt = joinedAt;
		this.uuid = player.getUniqueId();
		this.ip = player.getAddress().getAddress().getHostAddress();

		this.packetInjection = new PacketInjection(player);
		this.nettyDecodeInjection = new NettyDecodeInjection(player);
		this.packetLogger = new PacketLogger(player, this.packetInjection);

		initialize();
	}

	private void initialize() {
		this.packetLogger.startLogging();
		this.packetInjection.inject();
		AntiCrash.getInstance().getChecks().forEach(check -> check.registerFACPlayer(this));

		this.online = true;
	}

	public void unregister() {
		this.online = false;
		this.lastOnlineAt = System.currentTimeMillis();

		if (nettyDecodeInjection != null)
			nettyDecodeInjection.unInject();
	}

	public void setOnline() {
		this.online = true;
	}

	public void setLastOnline(long lastOnlineAt) {
		this.lastOnlineAt = lastOnlineAt;
		this.online = false;
	}

	public long getJoinedAt() {
		return joinedAt;
	}

	public long getLastOnlineAt() {
		return lastOnlineAt;
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getIp() {
		return ip;
	}

	public boolean isOnline() {
		return online;
	}

	public PacketInjection getPacketInjection() {
		return packetInjection;
	}

	public NettyDecodeInjection getNettyDecodeInjection() {
		return nettyDecodeInjection;
	}

	public PacketLogger getPacketLogger() {
		return packetLogger;
	}
}