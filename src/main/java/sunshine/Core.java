package core.sunshine;

import core.sunshine.anticrash.AntiCrash;
import core.sunshine.impl.DosCheck;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {

	private static Core plugin;

	public static Core getPlugin() {
		return plugin;
	}

	@Override
	public void onEnable() {
		plugin = this;

		AntiCrash.init(this);

		logInfo("§a§lSHIELD §fAntiCrash iniciado!");
		logInfo("§a§lSHIELD §fPlugin Habilitado! §a[" + getDescription().getVersion() + "]");
	}

	@Override
	public void onDisable() {
		DosCheck dosCheck = (DosCheck) AntiCrash.getInstance().getCheck(DosCheck.class);

		if (dosCheck != null) {
			dosCheck.getInjection().close();
			getLogger().info("Canais MC não injetados");
		}

		AntiCrash.getInstance().getPlayerCash().unregisterAll();

		logInfo("§c§lSHIELD §fPlugin desabilitado! §c[" + getDescription().getVersion() + "]");

		plugin = null;
	}

	private void logInfo(String message) {
		Bukkit.getConsoleSender().sendMessage(message);
	}
}