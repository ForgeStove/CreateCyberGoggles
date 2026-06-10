package io.github.forgestove.create_cyber_goggles.mixin.provider;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(EnderChestBlockEntity.class)
public abstract class EnderChestBlockEntityMixin implements ItemRenderable, Self<EnderChestBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		if (!CCG.config.tooltip.container) return null;
		return new ItemStack(thiz().getBlockState().getBlock());
	}
}
