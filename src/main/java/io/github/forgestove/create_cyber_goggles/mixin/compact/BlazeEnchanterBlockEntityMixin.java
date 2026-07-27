package io.github.forgestove.create_cyber_goggles.mixin.compact;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.*;
@Pseudo
@Mixin(BlazeEnchanterBlockEntity.class)
public abstract class BlazeEnchanterBlockEntityMixin implements ItemRenderable, Self<BlazeEnchanterBlockEntity> {
	@Unique public ItemStack ccg$cachedResult = ItemStack.EMPTY;
	@Unique public int ccg$lastEnchantLevel;
	@Shadow protected EnchanterBehaviour enchanter;
	@Shadow protected ItemStack heldItem;
	@Override
	public ItemStack ccg$getItemStack() {
		if (thiz().isActive()) {
			var copy = heldItem.copy();
			if (ccg$cachedResult.isEmpty() || enchanter.value != ccg$lastEnchantLevel) {
				ccg$cachedResult = enchanter.getResult(copy);
				ccg$lastEnchantLevel = enchanter.value;
			}
			return ccg$cachedResult;
		}
		ccg$cachedResult = ItemStack.EMPTY;
		return heldItem.copy();
	}
}
