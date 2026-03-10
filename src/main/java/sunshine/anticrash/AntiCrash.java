package core.sunshine.anticrash;

import core.sunshine.anticrash.player.PlayerCash;
import core.sunshine.anticrash.utils.TPSCalculator;
import core.sunshine.checks.ICheck;
import core.sunshine.config.ConfigCache;
import core.sunshine.impl.*;
import core.sunshine.impl.nbt.NBTTagCheck;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public final class AntiCrash {

    public static final String PREFIX = "§c§lSHIELD §c";
    
    private static AntiCrash instance;
    
    private final Plugin plugin;
    private final List<ICheck> checks = new ArrayList<>();
    private final TPSCalculator tpsCalculator;
    private final PlayerCash playerCash;

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            new AntiCrash(plugin);
        }
    }

    private AntiCrash(Plugin plugin) {
        instance = this;
        this.plugin = plugin;
        
        new ConfigCache();
        
        this.tpsCalculator = new TPSCalculator();
        this.playerCash = new PlayerCash();
        
        registerChecks();
        
        Bukkit.getOnlinePlayers().forEach(playerCash::register);
    }

    private void registerChecks() {
        plugin.getLogger().info("Registrando módulos de verificação...");
        
        checks.add(new BookCheck());
        checks.add(new WindowClickCheck());
        checks.add(new BlockPlaceCheck());
        checks.add(new MoveCheck());
        checks.add(new NBTTagCheck());
        checks.add(new InstantCrasherCheck());
        checks.add(new DosCheck());
        checks.add(new SignCheck());
        checks.add(new EnderPortalCheck());
        checks.add(new Log4JExploitCheck());
        checks.add(new ChatMessageCheck());
    }

    public ICheck getCheck(Class<? extends ICheck> clazz) {
        return checks.stream()
                .filter(check -> check.getClass().equals(clazz))
                .findFirst()
                .orElse(null);
    }

    public List<ICheck> getChecks() {
        return Collections.unmodifiableList(checks);
    }

    public static AntiCrash getInstance() {
        return instance;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public TPSCalculator getTpsCalculator() {
        return tpsCalculator;
    }

    public PlayerCash getPlayerCash() {
        return playerCash;
    }
}