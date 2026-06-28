package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.foundation.item.TooltipHelper;
import io.github.forgestove.create_cyber_goggles.CCG;
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

import java.util.List;
@Mixin(DeployerBlockEntity.class)
public abstract class DeployerBlockEntityMixin extends KineticBlockEntity {
	@Shadow(remap = false) protected List<ItemStack> overflowItems;
	public DeployerBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true, remap = false)
	public void addToTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.tooltip.deployer) return;
		if (overflowItems.isEmpty()) {
			cir.setReturnValue(false);
			return;
		}
		super.addToTooltip(tooltip, isPlayerSneaking);
		TooltipHelper.addHint(tooltip, "hint.full_deployer");
		CCGLang.translate("tooltip.content").style(ChatFormatting.GRAY).forGoggles(tooltip);
		CCGLang.itemList(overflowItems, 9).forGoggles(tooltip.size(), tooltip);
		cir.setReturnValue(true);
	}
	@Inject(
		method = "addToGoggleTooltip", at = @At(
		value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity;calculateStressApplied()F"
	), cancellable = true
	)
	public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.goggles.enhancedInfo) return;
		super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		cir.setReturnValue(true);
	}
}
