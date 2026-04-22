package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.AllItems;
import dev.simulated_team.simulated.index.SimItems;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.*;
public class LocalItemUtil {
	public static @Nullable ItemStack getItemStack(Player player, @NotNull EquipmentSlot slot) {
		if (CCGKey.useSchematic.isDown()) return AllItems.SCHEMATIC_AND_QUILL.asStack();
		if (CCGKey.showSuperGlue.isDown()) return AllItems.SUPER_GLUE.asStack();
		if (!CCGMods.SIMULATED.isLoaded()) return player.getItemBySlot(slot);
		if (CCGKey.usePhysicsStaff.isDown()) return SimItems.PHYSICS_STAFF.asStack();
		if (CCGKey.showHoneyGlue.isDown()) return SimItems.HONEY_GLUE.asStack();
		return null;
	}
}
