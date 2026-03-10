package core.sunshine.impl.nbt;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import core.sunshine.Core;
import core.sunshine.anticrash.utils.ExploitCheckUtils;
import core.sunshine.checks.CheckResult;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public final class MapNBTCheck implements INBTCheck, Listener {

    public MapNBTCheck() {
        Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
    }

    @Override
    public CheckResult isValid(NbtCompound tag) {
        if (tag == null) return new CheckResult.Positive("Tag nula.");
        return ExploitCheckUtils.isValidMap(tag);
    }

    @Override
    public List<Material> material() {
        return Arrays.asList(Material.MAP, Material.EMPTY_MAP);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.EMPTY_MAP) return;

        Bukkit.getScheduler().runTaskLater(Core.getPlugin(), () -> {
            if (!event.getPlayer().isOnline()) return;

            ItemStack currentItem = event.getPlayer().getItemInHand();
            
            if (currentItem != null && currentItem.getType() == Material.MAP) {
                try {
                    NbtCompound tag = NbtFactory.asCompound(NbtFactory.fromItemTag(currentItem));
                    CheckResult result = ExploitCheckUtils.isValidMap(tag);
                    if (result.check()) {
                        event.getPlayer().setItemInHand(null);
                        Bukkit.getLogger().warning("[Sunshine] Mapa malicioso removido de: " + event.getPlayer().getName());
                    }
                } catch (Exception e) {
                    event.getPlayer().setItemInHand(null);
                }
            }
        }, 1L);
    }
}