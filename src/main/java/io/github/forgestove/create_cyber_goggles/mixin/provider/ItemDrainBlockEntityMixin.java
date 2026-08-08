package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.fluids.drain.ItemDrainBlockEntity;
import io.github.forgestove.create_cyber_goggles.api.ItemRenderable;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(ItemDrainBlockEntity.class)
public abstract class ItemDrainBlockEntityMixin implements ItemRenderable, Self<ItemDrainBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		return thiz().getHeldItemStack();
	}
}
