package core.sunshine.anticrash.player;

import core.sunshine.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerCash {

	private final Map<UUID, PlayerRegister> players = new ConcurrentHashMap<>();

	public PlayerCash() {

        Bukkit.getScheduler().runTaskTimerAsynchronously(Core.getPlugin(), () -> {
            long now = System.currentTimeMillis();
            
            players.values().removeIf(player -> {
                player.setLastOnline(now);
                return player.getLastOnlineAt() + 60000L < now;
            });
            
        }, 0L, 20L);
    }

	public void register(Player bukkitPlayer) {
		UUID uuid = bukkitPlayer.getUniqueId();

		PlayerRegister player = players.computeIfAbsent(uuid,
				k -> new PlayerRegister(System.currentTimeMillis(), bukkitPlayer));

		player.setOnline();
	}

	public void handleQuit(UUID uuid) {
		PlayerRegister player = players.get(uuid);
		if (player != null) {
			player.unregister();
			player.setLastOnline(System.currentTimeMillis());
		}
	}

	public PlayerRegister getPlayer(UUID uuid) {
		return players.get(uuid);
	}

	public PlayerRegister getPlayerByIP(String ip) {
		return players.values().stream().filter(player -> player.getIp().equals(ip)).findFirst().orElse(null);
	}

	public void unregisterAll() {
		players.values().forEach(PlayerRegister::unregister);
		players.clear();
	}
}