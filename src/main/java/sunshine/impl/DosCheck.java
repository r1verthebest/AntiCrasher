package core.sunshine.impl;

import core.sunshine.Core;
import core.sunshine.anticrash.utils.ActionbarUtil;
import core.sunshine.checks.AbstractCheck;
import core.sunshine.config.ConfigCache;
import core.sunshine.nettyinjections.MCChannelInjection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DosCheck extends AbstractCheck {

	private final CopyOnWriteArrayList<Connection> connections = new CopyOnWriteArrayList<>();
	private final Map<String, Long> blockedIPs = new ConcurrentHashMap<>();

	private MCChannelInjection injection;

	public boolean isAttack = false;

	private long attackStartedAt;
	private int cpsToUnlock;
	private int connectionPerSecond = 0;
	public long blockedConnections = 0L;
	private long lastBlockMessageSent = 0L;

	private final int cpsLimit = ConfigCache.getInstance().getValue("doscheck.cpsLimit", 130, Integer.class);
	private final long blockTime = ConfigCache.getInstance().getValue("doscheck.blocktime", 300000, Integer.class)
			.longValue();
	private final boolean debug = ConfigCache.getInstance().getValue("debugMode", false, Boolean.class);

	public DosCheck() {
		super("DosCheck");
		if (!isEnable())
			return;

		try {
			this.injection = new MCChannelInjection(this);
		} catch (Exception e) {
			Core.getPlugin().getLogger().severe("Falha ao injetar MCChannelInjection: " + e.getMessage());
			return;
		}

		Bukkit.getScheduler().runTaskTimerAsynchronously(Core.getPlugin(), this::runMonitorTask, 20L, 20L);
	}

	public MCChannelInjection getInjection() {
		return injection;
	}

	private void runMonitorTask() {
		long now = System.currentTimeMillis();

		this.connectionPerSecond = (int) connections.stream().filter(c -> now - c.getConnectedAt() < 1000L).count();

		connections.removeIf(c -> now - c.getConnectedAt() > 5000L);
		if (!blockedIPs.isEmpty()) {
			blockedIPs.entrySet().removeIf(entry -> now - entry.getValue() > blockTime);
		}

		if (!isAttack && connectionPerSecond > cpsLimit) {
			enableAttackMode(cpsLimit);
		} else if (isAttack && connectionPerSecond <= cpsToUnlock && (now - attackStartedAt) > 10000L) {
			endAttackMode();
		}

		updateVisualFeedback();
	}

	public boolean initChannel(ChannelHandlerContext ctx) {
		String host = getHostFromCtx(ctx);
		if (host == null)
			return false;

		long now = System.currentTimeMillis();
		connections.add(new Connection(host, now));

		if (blockedIPs.containsKey(host)) {
			blockedConnections++;
			return false;
		}

		long connectionsFromIP = connections.stream()
				.filter(c -> c.getHost().equals(host) && (now - c.getConnectedAt() < 1000L)).count();

		int maxCpsPerIp = ConfigCache.getInstance().getValue("doscheck.cps_limit_ip", 20, Integer.class);
		if (connectionsFromIP > maxCpsPerIp) {
			blockIP(host, "Excedeu limite de conexões por segundo (" + maxCpsPerIp + ")");
			return false;
		}

		return true;
	}

	public boolean handleObject(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof ByteBuf) {
			int capacity = ((ByteBuf) msg).capacity();
			int maxCap = ConfigCache.getInstance().getValue("doscheck.maxDataCapacity", 1000, Integer.class);

			if (capacity > maxCap) {
				String host = getHostFromCtx(ctx);
				blockIP(host, "Packet size exploit (> " + maxCap + ")");
				return false;
			}
		}
		return true;
	}

	public void blockIP(String ip, String reason) {
		if (ip == null || ip.equals("127.0.0.1"))
			return;

		blockedIPs.put(ip, System.currentTimeMillis());

		if (System.currentTimeMillis() - lastBlockMessageSent > 5000L) {
			lastBlockMessageSent = System.currentTimeMillis();
			String alert = "§c§lSHIELD §7IP Bloqueado: §f" + ip + " §7Motivo: §e" + reason;
			Bukkit.getConsoleSender().sendMessage(alert);

			Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("anticrash.notify"))
					.forEach(p -> p.sendMessage(alert));
		}
	}

	private void updateVisualFeedback() {
		if (!debug && !isAttack)
			return;

		String layout = isAttack ? "&c&lATAQUE &7| &fCPS: &c%cps% &7| &fIPs: &c%ips% &7| &fBloqueios: &c%blocked%"
				: "&f&lSHIELD &7| &fCPS: &a%cps% &7| &fIPs Bloqueados: &a%ips%";

		String message = ChatColor.translateAlternateColorCodes('&',
				layout.replace("%cps%", String.valueOf(connectionPerSecond))
						.replace("%ips%", String.valueOf(blockedIPs.size()))
						.replace("%blocked%", String.valueOf(blockedConnections)));

		Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("anticrash.notify"))
				.forEach(p -> ActionbarUtil.sendActionbar(p, message));
	}

	private String getHostFromCtx(ChannelHandlerContext ctx) {
		try {
			return ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
		} catch (Exception e) {
			return null;
		}
	}

	public void enableAttackMode(int unlockCps) {
		if (isAttack)
			return;
		this.isAttack = true;
		this.cpsToUnlock = unlockCps;
		this.attackStartedAt = System.currentTimeMillis();
		Bukkit.broadcast("§c§lSHIELD §fModo de ataque ativado! Filtragem de rede reforçada.", "anticrash.notify");
	}

	public void endAttackMode() {
		this.isAttack = false;
		Bukkit.broadcast("§a§lSHIELD §fO ataque cessou. O servidor está estável.", "anticrash.notify");
	}

	public static class Connection {
		private final String host;
		private final long connectedAt;

		public Connection(String host, long connectedAt) {
			this.host = host;
			this.connectedAt = connectedAt;
		}

		public String getHost() {
			return host;
		}

		public long getConnectedAt() {
			return connectedAt;
		}
	}
}