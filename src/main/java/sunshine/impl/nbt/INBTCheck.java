package core.sunshine.impl.nbt;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import core.sunshine.checks.CheckResult;
import org.bukkit.Material;

import java.util.List;

public interface INBTCheck {
	CheckResult isValid(NbtCompound nbt);

	List<Material> material();
}