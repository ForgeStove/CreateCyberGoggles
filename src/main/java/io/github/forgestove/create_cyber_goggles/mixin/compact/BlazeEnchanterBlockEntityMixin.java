package io.github.forgestove.create_cyber_goggles.mixin.compact;
import io.github.forgestove.create_cyber_goggles.core.api.ItemRenderable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.*;
@Pseudo
@Mixin(BlazeEnchanterBlockEntity.class)
public abstract class BlazeEnchanterBlockEntityMixin implements ItemRenderable {
	@Unique public ItemStack ccg$cachedResult = ItemStack.EMPTY;
	@Shadow protected EnchanterBehaviour enchanter;
	@Shadow protected ItemStack heldItem;
	@Override
	public ItemStack ccg$getItemStack() {
		if (ccg$cachedResult.isEmpty()) ccg$cachedResult = enchanter.getResult(heldItem.copy());
		if (isActive()) return ccg$cachedResult;
		ccg$cachedResult = ItemStack.EMPTY;
		return heldItem;
	}
	@Shadow
	public abstract boolean isActive();
}
