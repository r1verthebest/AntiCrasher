package core.sunshine.config;

import core.sunshine.Core;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigCache {

	private static ConfigCache instance;

	private final FileConfiguration config;
	private final Map<String, Object> cache = new ConcurrentHashMap<>();

	public ConfigCache() {
		instance = this;

		Core plugin = Core.getPlugin();
		plugin.saveDefaultConfig();

		this.config = plugin.getConfig();
	}

	public <T> T getValue(String key, T defaultValue, Class<T> type) {
		Object value = cache.computeIfAbsent(key, k -> {
			Object found = config.get(k);
			return (found != null) ? found : defaultValue;
		});

		try {
			return type.isInstance(value) ? type.cast(value) : defaultValue;
		} catch (ClassCastException e) {
			return defaultValue;
		}
	}

	public boolean isCheckEnable(String checkName) {
		String path = "checks." + checkName;

		if (!config.contains(path)) {
			config.set(path, true);
			return true;
		}

		return config.getBoolean(path);
	}

	public boolean contains(String path) {
		return config.contains(path);
	}

	public static ConfigCache getInstance() {
		return instance;
	}
}