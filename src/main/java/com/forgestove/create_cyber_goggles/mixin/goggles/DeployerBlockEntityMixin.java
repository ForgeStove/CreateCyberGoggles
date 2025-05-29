package com.forgestove.create_cyber_goggles.mixin.goggles;
import com.forgestove.create_cyber_goggles.CCG;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(value = DeployerBlockEntity.class, remap = false)
public abstract class DeployerBlockEntityMixin extends KineticBlockEntity {
	@Shadow protected List<ItemStack> overflowItems;
	public DeployerBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
	private void addToTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> returnable) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		super.addToTooltip(tooltip, isPlayerSneaking);
		if (overflowItems.isEmpty()) {
			returnable.setReturnValue(false);
			return;
		}
		TooltipHelper.addHint(tooltip, "hint.full_deployer");
		for (var itemStack : overflowItems)
			CreateLang.builder()
				.add(Component.translatable(itemStack.getDescriptionId()).withStyle(ChatFormatting.GRAY))
				.add(CreateLang.text(" x" + itemStack.getCount()).style(ChatFormatting.GREEN))
				.forGoggles(tooltip);
		returnable.setReturnValue(true);
	}
	@Inject(
		method = "addToGoggleTooltip", at = @At(
		value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity;calculateStressApplied()F"
	), cancellable = true
	)
	private void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> returnable) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		returnable.setReturnValue(super.addToGoggleTooltip(tooltip, isPlayerSneaking));
	}
}
