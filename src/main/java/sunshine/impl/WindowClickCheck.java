package core.sunshine.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import core.sunshine.anticrash.utils.ExploitCheckUtils;
import core.sunshine.checks.AbstractCheck;
import core.sunshine.checks.CheckResult;
import core.sunshine.config.ConfigCache;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

public final class WindowClickCheck extends AbstractCheck {

	public WindowClickCheck() {
		super("WindowClickCheck", PacketType.Play.Client.WINDOW_CLICK, PacketType.Play.Client.SET_CREATIVE_SLOT);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		PacketContainer packet = event.getPacket();
		ItemStack bukkitStack = packet.getItemModifier().readSafely(0);

		if (bukkitStack == null || bukkitStack.getType() == Material.AIR) {
			return;
		}

		Material type = bukkitStack.getType();
		if (type == Material.WRITTEN_BOOK || type == Material.BOOK_AND_QUILL) {
			if (ConfigCache.getInstance().getValue("bookcheck.disableBooks", false, Boolean.class)) {
				event.setCancelled(true);
				return;
			}

			net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(bukkitStack);

			if (nmsStack != null && nmsStack.hasTag()) {
				CheckResult result = ExploitCheckUtils.isInvalidBookTag(nmsStack.getTag());

				if (result.check()) {
					event.setCancelled(true);
					sendCrashWarning(event.getPlayer(), event, result.getReason());
				}
			}
		}
	}
}