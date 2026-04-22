package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.AllItems;
import dev.simulated_team.simulated.index.SimItems;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
public class LocalItemUtil {
	public static @Nullable ItemStack getCreate() {
		if (CCGKey.useSchematic.isDown()) return AllItems.SCHEMATIC_AND_QUILL.asStack();
		if (CCGKey.showSuperGlue.isDown()) return AllItems.SUPER_GLUE.asStack();
		return null;
	}
	public static @Nullable ItemStack getSimulated() {
		if (CCGKey.usePhysicsStaff.isDown()) return SimItems.PHYSICS_STAFF.asStack();
		if (CCGKey.showHoneyGlue.isDown()) return SimItems.HONEY_GLUE.asStack();
		return null;
	}
}
