package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(BeltBlockEntity.class)
public abstract class BeltBlockEntityMixin implements IItemRenderable {
	@Shadow public int index;
	@Shadow protected BeltInventory inventory;
	@Override
	public ItemStack ccg$getItemStack() {
		if (inventory == null) return null;
		var stackAtOffset = inventory.getStackAtOffset(index);
		return stackAtOffset == null ? null : stackAtOffset.stack;
	}
}
