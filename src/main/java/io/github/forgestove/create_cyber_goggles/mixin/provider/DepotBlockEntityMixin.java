package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.logistics.depot.DepotBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(DepotBlockEntity.class)
public abstract class DepotBlockEntityMixin implements ItemRenderable, Self<DepotBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		var heldItem = self().getHeldItem();
		return heldItem == null ? null : heldItem.stack;
	}
}
