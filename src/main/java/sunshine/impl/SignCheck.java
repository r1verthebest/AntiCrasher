package core.sunshine.impl;

import core.sunshine.Core;
import core.sunshine.checks.AbstractCheck;
import core.sunshine.config.ConfigCache;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public final class SignCheck extends AbstractCheck implements Listener {

	public SignCheck() {
		super("SignCheck");
		if (isEnable()) {
			Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onSignUpdate(SignChangeEvent event) {
		int maxLength = ConfigCache.getInstance().getValue("signcheck.maxLength", 50, Integer.class);

		for (String line : event.getLines()) {
			if (line == null)
				continue;

			if (line.length() > maxLength || containsIllegalChars(line)) {
				event.setCancelled(true);
				sendCrashWarning(event.getPlayer(), null, "Tentativa de Sign Exploit (Texto excessivo ou malformado).");
				return;
			}
		}
	}

	private boolean containsIllegalChars(String text) {
		if (text.chars().anyMatch(c -> c < 32 && c != 167))
			return true;
		return text.getBytes().length > (text.length() * 2);
	}
}