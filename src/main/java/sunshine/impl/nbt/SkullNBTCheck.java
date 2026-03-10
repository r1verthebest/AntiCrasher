package core.sunshine.impl.nbt;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import core.sunshine.anticrash.utils.ExploitCheckUtils;
import core.sunshine.checks.CheckResult;
import org.bukkit.Material;

import java.util.Collections;
import java.util.Arrays;
import java.util.List;

public class SkullNBTCheck implements INBTCheck {

	private static final List<Material> MATERIALS = Collections
			.unmodifiableList(Arrays.asList(Material.SKULL, Material.SKULL_ITEM));

	@Override
	public CheckResult isValid(NbtCompound tag) {
		if (tag == null) {
			return new CheckResult.Negative("");
		}

		CheckResult result = ExploitCheckUtils.isInvalidSkull(tag);
		return result;
	}

	@Override
	public List<Material> material() {
		return MATERIALS;
	}
}