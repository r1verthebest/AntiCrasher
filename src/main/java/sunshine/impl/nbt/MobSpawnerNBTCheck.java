package core.sunshine.impl.nbt;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import core.sunshine.anticrash.utils.ExploitCheckUtils;
import core.sunshine.checks.CheckResult;
import org.bukkit.Material;

import java.util.Collections;
import java.util.List;

public class MobSpawnerNBTCheck implements INBTCheck {

	@Override
	public CheckResult isValid(NbtCompound spawnerTag) {
		if (spawnerTag == null) {
			return new CheckResult.Negative("");
		}

		if (spawnerTag.containsKey("BlockEntityTag")) {
			return ExploitCheckUtils.isValidSpawnerEntityTag(spawnerTag.getCompound("BlockEntityTag"));
		}

		if (spawnerTag.containsKey("EntityTag")) {
			return ExploitCheckUtils.isValidSpawnerEntityTag(spawnerTag.getCompound("EntityTag"));
		}

		return new CheckResult.Negative("");
	}

	@Override
	public List<Material> material() {
		return Collections.singletonList(Material.MOB_SPAWNER);
	}
}