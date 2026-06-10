package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.logistics.depot.DepotBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(DepotBlockEntity.class)
public abstract class DepotBlockEntityMixin implements ItemRenderable, Self<DepotBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		if (!CCG.config.tooltip.depot) return null;
		var heldItem = thiz().getHeldItem();
		return heldItem == null ? null : heldItem.stack;
	}
}
