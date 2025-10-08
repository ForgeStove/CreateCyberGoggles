package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.*;
@Mixin(value = MillstoneBlockEntity.class, remap = false)
public abstract class MillstoneBlockEntityMixin implements IItemRenderable {
	@Shadow public ItemStackHandler inputInv;
	@Override
	public ItemStack ccg$getItemStack() {
		return inputInv.getStackInSlot(0);
	}
}
