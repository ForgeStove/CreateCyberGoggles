package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.logistics.depot.DepotBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
@Mixin(value = DepotBlockEntity.class, remap = false)
public abstract class DepotBlockEntityMixin implements IItemRenderable {
	@Shadow
	public abstract @Nullable TransportedItemStack getHeldItem();
	@Override
	public ItemStack ccg$getItemStack() {
		var heldItem = getHeldItem();
		return heldItem == null ? null : heldItem.stack;
	}
}
