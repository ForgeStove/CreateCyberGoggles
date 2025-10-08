package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(value = DepotBlockEntity.class, remap = false)
public abstract class DepotBlockEntityMixin implements IItemRenderable {
	@Shadow
	public abstract ItemStack getHeldItem();
	@Override
	public ItemStack ccg$getItemStack() {
		return getHeldItem();
	}
}
