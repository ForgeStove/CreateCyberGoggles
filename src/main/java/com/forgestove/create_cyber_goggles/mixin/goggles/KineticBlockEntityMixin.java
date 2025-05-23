package com.forgestove.create_cyber_goggles.mixin.goggles;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.simibubi.create.content.kinetics.base.IRotate.*;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.gauge.*;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(KineticBlockEntity.class)
public abstract class KineticBlockEntityMixin extends SmartBlockEntity {
	@Shadow protected float stress;
	@Shadow protected float capacity;
	@Shadow protected boolean overStressed;
	public KineticBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
	private void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> returnable) {
		var goggles = CCGConfig.config.goggles;
		if (!goggles.enhancedInfo) return;
		var hide = !goggles.hideStaticKineticInfo || !Mth.equal(getTheoreticalSpeed(), 0);
		returnable.setReturnValue(hide);
		if (!hide) return;
		if (StressImpact.isEnabled()) {
			var stressAtBase = calculateStressApplied();
			if (!Mth.equal(stressAtBase, 0)) addStressImpactStats(tooltip, stressAtBase);
		}
		CreateLang.translate("gui.goggles.kinetic_stats").forGoggles(tooltip);
		SpeedLevel.getFormattedSpeedText(getTheoreticalSpeed(), overStressed).forGoggles(tooltip);
		if (!CCGConfig.config.goggles.showNetworkStress) return;
		double stressFraction = stress / (capacity == 0 ? 1 : capacity);
		CreateLang.translate("gui.stressometer.title").style(ChatFormatting.GRAY).forGoggles(tooltip);
		if (getTheoreticalSpeed() == 0)
			CreateLang.text(TooltipHelper.makeProgressBar(3, 0)).translate("gui.stressometer.no_rotation").style(ChatFormatting.DARK_GRAY)
					  .forGoggles(tooltip);
		else {
			StressImpact.getFormattedStressText(stressFraction).forGoggles(tooltip);
			CreateLang.translate("gui.stressometer.capacity").style(ChatFormatting.GRAY).forGoggles(tooltip);
			double remainingCapacity = capacity - stress;
			var su = CreateLang.translate("generic.unit.stress");
			var stressTip = CreateLang.number(remainingCapacity).add(su).style(StressImpact.of(stressFraction).getRelativeColor());
			if (remainingCapacity != capacity)
				stressTip.text(ChatFormatting.GRAY, " / ").add(CreateLang.number(capacity).add(su).style(ChatFormatting.DARK_GRAY));
			stressTip.forGoggles(tooltip, 1);
		}
		if (!worldPosition.equals(StressGaugeBlockEntity.lastSent))
			CatnipServices.NETWORK.sendToServer(new GaugeObservedPacket(StressGaugeBlockEntity.lastSent = worldPosition));
	}
	@Shadow
	public abstract float getTheoreticalSpeed();
	@Shadow
	public abstract float calculateStressApplied();
	@Shadow
	protected abstract void addStressImpactStats(List<Component> tooltip, float stressAtBase);
}
