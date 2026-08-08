package io.github.forgestove.create_cyber_goggles.mixin.provider;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.ItemRenderable;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin implements ItemRenderable, Self<ItemEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		if (!CCG.config.tooltip.itemEntity) return null;
		return thiz().getItem();
	}
}
