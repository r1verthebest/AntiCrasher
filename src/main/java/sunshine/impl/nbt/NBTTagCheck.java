package core.sunshine.impl.nbt;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import core.sunshine.checks.AbstractCheck;
import core.sunshine.checks.CheckResult;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NBTTagCheck extends AbstractCheck {

	private final List<INBTCheck> checks = new ArrayList<>();

	public NBTTagCheck() {
		super("NBTTagCheck", PacketType.Play.Client.BLOCK_PLACE, PacketType.Play.Client.SET_CREATIVE_SLOT);
		this.checks.add(new MobSpawnerNBTCheck());
		this.checks.add(new SkullNBTCheck());
		this.checks.add(new MapNBTCheck());
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		Player player = event.getPlayer();
		PacketContainer packet = event.getPacket();

		if (packet.getType() == PacketType.Play.Client.SET_CREATIVE_SLOT) {
			if (player.getGameMode() != GameMode.CREATIVE) {
				this.sendCrashWarning(player, event, "Tentativa de SetCreativeSlot sem estar no criativo");
				event.setCancelled(true);
				return;
			}
		}

		ItemStack stack = packet.getItemModifier().readSafely(0);
		if (stack == null || stack.getType() == Material.AIR) {
			return;
		}

		Material itemType = stack.getType();

		try {
			NbtCompound tag = NbtFactory.asCompound(NbtFactory.fromItemTag(stack));
			if (tag == null)
				return;

			for (INBTCheck check : checks) {
				if (check.material().contains(itemType)) {
					CheckResult result = check.isValid(tag);

					if (result.check()) {
						player.setItemInHand(null);

						this.sendCrashWarning(player, event, "Tag NBT inválida: " + result.getReason());
						event.setCancelled(true);
						break;
					}
				}
			}
		} catch (Exception e) {
			event.setCancelled(true);
		}
	}

	public List<INBTCheck> getChecks() {
		return Collections.unmodifiableList(this.checks);
	}
}