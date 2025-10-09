package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.kinetics.millstone.MillstoneBlockEntity;
import com.zurrtum.create.content.kinetics.millstone.MillstoneBlockEntity.MillstoneInventoryHandler;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(value = MillstoneBlockEntity.class, remap = false)
public abstract class MillstoneBlockEntityMixin implements IItemRenderable {
	@Shadow public MillstoneInventoryHandler capability;
	@Override
	public ItemStack ccg$getItemStack() {
		return capability.getItem(0);
	}
}
