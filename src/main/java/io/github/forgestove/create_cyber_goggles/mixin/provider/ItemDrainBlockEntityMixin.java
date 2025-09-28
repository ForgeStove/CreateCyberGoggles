package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.fluids.drain.ItemDrainBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(ItemDrainBlockEntity.class)
public abstract class ItemDrainBlockEntityMixin implements IItemRenderable {
	@Shadow
	public abstract ItemStack getHeldItemStack();
	@Override
	public ItemStack ccg$getItemStack() {
		return getHeldItemStack();
	}
}
