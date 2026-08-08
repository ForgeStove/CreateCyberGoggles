package io.github.forgestove.create_cyber_goggles.mixin.provider;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.ItemRenderable;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(EnderChestBlockEntity.class)
public abstract class EnderChestBlockEntityMixin implements ItemRenderable, Self<EnderChestBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		return CCG.config.tooltip.container ? new ItemStack(thiz().getBlockState().getBlock()) : null;
	}
}
