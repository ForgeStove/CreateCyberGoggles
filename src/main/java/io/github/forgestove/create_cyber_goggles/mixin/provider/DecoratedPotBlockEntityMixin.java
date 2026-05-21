package io.github.forgestove.create_cyber_goggles.mixin.provider;
import io.github.forgestove.create_cyber_goggles.core.api.ItemRenderable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import org.spongepowered.asm.mixin.*;
@Mixin(DecoratedPotBlockEntity.class)
public abstract class DecoratedPotBlockEntityMixin implements ItemRenderable {
	@Shadow private ItemStack item;
	@Override
	public ItemStack ccg$getItemStack() {
		return item;
	}
}
