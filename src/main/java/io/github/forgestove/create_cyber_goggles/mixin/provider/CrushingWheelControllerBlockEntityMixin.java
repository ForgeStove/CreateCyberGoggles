package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingInventory;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(value = CrushingWheelControllerBlockEntity.class, remap = false)
public abstract class CrushingWheelControllerBlockEntityMixin implements IItemRenderable {
	@Shadow public ProcessingInventory inventory;
	@Override
	public ItemStack ccg$getItemStack() {
		return inventory.getStackInSlot(0);
	}
}
