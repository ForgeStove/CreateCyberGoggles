package io.github.forgestove.create_cyber_goggles.mixin.compact;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createenchantmentindustry.common.fluids.experience.BlazeExperienceBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.*;

import java.util.List;
@Pseudo
@Mixin(BlazeEnchanterBlockEntity.class)
public abstract class BlazeEnchanterBlockEntityMixin extends BlazeExperienceBlockEntity
	implements ItemRenderable, Self<BlazeEnchanterBlockEntity> {
	@Unique public ItemStack ccg$cachedResult = ItemStack.EMPTY;
	@Unique public int ccg$lastEnchantLevel;
	@Shadow protected EnchanterBehaviour enchanter;
	@Shadow protected ItemStack heldItem;
	@Shadow protected int processingTime;
	public BlazeEnchanterBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
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
	@Inject(method = "addToGoggleTooltip", at = @At("TAIL"))
	public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.goggles.enhancedInfo || processingTime <= 0) return;
		CCGLang.add(Component.translatable("create_cyber_goggles.tooltip.leftTime").withStyle(ChatFormatting.GRAY))
			.number(processingTime / 20, ChatFormatting.GOLD)
			.space()
			.seconds(ChatFormatting.GRAY)
			.forGoggles(tooltip);
	}
}
