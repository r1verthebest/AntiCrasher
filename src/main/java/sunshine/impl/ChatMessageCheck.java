package core.sunshine.impl;

import core.sunshine.Core;
import core.sunshine.checks.AbstractCheck;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class ChatMessageCheck extends AbstractCheck implements Listener {

	private final Map<UUID, Long> lastMessage = new ConcurrentHashMap<>();
	private static final long THRESHOLD_MS = 100L;

	private static final Pattern WORLDEDIT_EXPLOIT = Pattern.compile(".*[()\\[\\]{}\\?:;].*");
	private static final Pattern BAD_CHARS = Pattern.compile(".*(\n|\\.\\*\\.\\*|̇).*");

	public ChatMessageCheck() {
		super("ChatMessageCheck");
		Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void handleChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		long now = System.currentTimeMillis();

		Long last = lastMessage.get(uuid);
		if (last != null && (now - last) < THRESHOLD_MS) {
			event.setCancelled(true);
			this.sendCrashWarning(player, null, "Flood de mensagens detectado (Chat).");
			return;
		}

		lastMessage.put(uuid, now);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void handlePreCommand(PlayerCommandPreprocessEvent event) {
		String msg = event.getMessage().toLowerCase();
		Player player = event.getPlayer();

		if (isWorldEditRisk(msg)) {
			event.setCancelled(true);
			this.sendCrashWarning(player, null, "Tentativa de exploit via WorldEdit (Calc/Eval).");
			return;
		}

		if (msg.startsWith("/mv")) {
			if (BAD_CHARS.matcher(msg).matches() || msg.contains("/") || msg.contains("\\")) {
				event.setCancelled(true);
				this.sendCrashWarning(player, null, "Exploit de comando detectado (MV Path/Char).");
				return;
			}
		}

		if (msg.contains("pex promote") || msg.contains("pex demote")) {
			if (msg.contains(" a a") || msg.length() > 100) {
				event.setCancelled(true);
				this.sendCrashWarning(player, null, "Tentativa de crash no PEX detectada.");
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		lastMessage.remove(event.getPlayer().getUniqueId());
	}

	private boolean isWorldEditRisk(String msg) {
		if (msg.startsWith("/calc") || msg.startsWith("/solve") || msg.startsWith("/eval") || msg.startsWith("/desc")) {
			return msg.length() > 25 || WORLDEDIT_EXPLOIT.matcher(msg).matches();
		}
		return false;
	}
}