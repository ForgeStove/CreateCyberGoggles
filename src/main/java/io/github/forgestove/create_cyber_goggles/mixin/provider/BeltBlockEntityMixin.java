package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(value = BeltBlockEntity.class, remap = false)
public abstract class BeltBlockEntityMixin implements IItemRenderable {
	@Shadow public int index;
	@Shadow
	public abstract BeltBlockEntity getControllerBE();
	@Override
	public ItemStack ccg$getItemStack() {
		var controllerBE = getControllerBE();
		if (controllerBE == null) return null;
		var inventory = controllerBE.getInventory();
		if (inventory == null) return null;
		var stackAtOffset = inventory.getStackAtOffset(index);
		return stackAtOffset == null ? null : stackAtOffset.stack;
	}
}
