package core.sunshine.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerKickEvent;

import core.sunshine.anticrash.AntiCrash;

public class PlayerConnectionListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		AntiCrash.getInstance().getPlayerCash().register(player);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		cleanup(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onKick(PlayerKickEvent event) {
		cleanup(event.getPlayer());
	}

	private void cleanup(Player player) {
		if (player == null)
			return;
		AntiCrash.getInstance().getPlayerCash().handleQuit(player.getUniqueId());
	}
}