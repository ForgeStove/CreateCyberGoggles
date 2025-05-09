package com.forgestove.create_cyber_goggles.mixin.goggles;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(GeneratingKineticBlockEntity.class)
public abstract class GeneratingKineticBlockEntityMixin extends KineticBlockEntity {
	public GeneratingKineticBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), remap = false, cancellable = true)
	private void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> returnable) {
		var goggles = CCGConfig.get().goggles;
		if (!goggles.enhancedInfo) return;
		var speed = getTheoreticalSpeed();
		if (goggles.hideStaticKineticInfo && speed == 0) {
			returnable.setReturnValue(false);
			return;
		}
		var stressBase = calculateAddedStressCapacity();
		if (!Mth.equal(stressBase, 0)) {
			Lang.translate("gui.goggles.generator_stats").forGoggles(tooltip);
			Lang.translate("tooltip.capacityProvided").style(ChatFormatting.GRAY).forGoggles(tooltip);
			if (speed != getGeneratedSpeed()) stressBase *= getGeneratedSpeed() / speed;
			Lang.number(Math.abs(stressBase * speed)).translate("generic.unit.stress").style(ChatFormatting.AQUA).space()
				.add(Lang.translate("gui.goggles.at_current_speed").style(ChatFormatting.DARK_GRAY)).forGoggles(tooltip);
		}
		returnable.setReturnValue(super.addToGoggleTooltip(tooltip, isPlayerSneaking));
	}
}
