package core.sunshine.impl;

import core.sunshine.Core;
import core.sunshine.checks.AbstractCheck;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public final class EnderPortalCheck extends AbstractCheck implements Listener {

	public EnderPortalCheck() {
		super("EnderPortalCrashCheck");
		if (isEnable()) {
			Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent event) {
		if (event.getCause() == TeleportCause.END_PORTAL) {
			event.setCancelled(true);
			if (event.getTo() == null)
				return;
			Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
				if (event.getPlayer().isOnline()) {
					event.getPlayer().teleport(event.getTo(), TeleportCause.PLUGIN);
				}
			}, 3L);
		}
	}
}