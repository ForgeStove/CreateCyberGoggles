package io.github.forgestove.create_cyber_goggles.mixin.provider;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import plus.dragons.createenchantmentindustry.common.fluids.experience.BlazeExperienceBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.*;
@Mixin(BlazeEnchanterBlockEntity.class)
public abstract class BlazeEnchanterBlockEntityMixin extends BlazeExperienceBlockEntity implements IItemRenderable {
	@Unique public ItemStack ccg$cachedResult = ItemStack.EMPTY;
	@Shadow protected EnchanterBehaviour enchanter;
	@Shadow protected ItemStack heldItem;
	public BlazeEnchanterBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	@Shadow
	public abstract boolean isActive();
	@Override
	public ItemStack ccg$getItemStack() {
		if (ccg$cachedResult.isEmpty()) ccg$cachedResult = enchanter.getResult(heldItem.copy());
		if (isActive()) return ccg$cachedResult;
		ccg$cachedResult = ItemStack.EMPTY;
		return heldItem;
	}
}
