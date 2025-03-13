package com.ForgeStove.create_cyber_goggles.mixin;
import com.simibubi.create.content.kinetics.base.*;
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
@Mixin(DeployerBlockEntity.class) public abstract class DeployerBlockEntityMixin extends KineticBlockEntity {
	@Shadow protected List<ItemStack> overflowItems;
	public DeployerBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
	private void addToTooltip(
			List<Component> tooltip,
			boolean isPlayerSneaking,
			CallbackInfoReturnable<Boolean> returnable
	) {
		super.addToTooltip(tooltip, isPlayerSneaking);
		if (getSpeed() == 0 || overflowItems.isEmpty()) {
			returnable.setReturnValue(false);
			return;
		}
		TooltipHelper.addHint(tooltip, "hint.full_deployer");
		for (ItemStack itemStack : overflowItems)
			CreateLang.translate(
					"tooltip.deployer.contains",
					Component.translatable(itemStack.getDescriptionId()).getString(),
					itemStack.getCount()
			).style(ChatFormatting.GREEN).forGoggles(tooltip);
		returnable.setReturnValue(true);
	}
	@Inject(method = "addToGoggleTooltip", at = @At("RETURN"))
	private void addToGoggleTooltip(
			List<Component> tooltip,
			boolean isPlayerSneaking,
			CallbackInfoReturnable<Boolean> returnable
	) {
		IRotate.SpeedLevel.getFormattedSpeedText(getSpeed(), overStressed).forGoggles(tooltip);
	}
}
