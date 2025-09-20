package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.*;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.base.IRotate.StressImpact;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.event.CCGKey;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.KineticBlockEntityAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(value = KineticTooltipBehaviour.class, remap = false)
public abstract class KineticBlockEntityMixin<T extends KineticBlockEntity> extends TooltipBehaviour<T> {
	public KineticBlockEntityMixin(T be) {
		super(be);
	}
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
	public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> returnable) {
		var goggles = CCG.CONFIG.goggles;
		if (!goggles.enhancedInfo) return;
		var hide = !goggles.hideStaticKineticInfo || !Mth.equal(blockEntity.getTheoreticalSpeed(), 0);
		returnable.setReturnValue(hide);
		if (!hide) return;
		if (StressImpact.isEnabled()) {
			var stressAtBase = blockEntity.calculateStressApplied();
			if (!Mth.equal(stressAtBase, 0)) addStressImpactStats(tooltip, stressAtBase);
		}
		CreateLang.translate("gui.goggles.kinetic_stats").forGoggles(tooltip);
		SpeedGaugeTooltipBehaviour.getFormattedSpeedText(blockEntity.getTheoreticalSpeed(), blockEntity.isOverStressed())
			.forGoggles(tooltip);
		if (!CCGKey.showStress.keyMapping.isDown()) return;
		var accessor = (KineticBlockEntityAccessor) blockEntity;
		var stress = accessor.getStress();
		var capacity = accessor.getCapacity();
		double stressFraction = stress / (capacity == 0 ? 1 : capacity);
		CreateLang.translate("gui.stressometer.title").style(ChatFormatting.GRAY).forGoggles(tooltip);
		if (blockEntity.getTheoreticalSpeed() == 0) CreateLang.text(TooltipHelper.makeProgressBar(3, 0))
			.translate("gui.stressometer.no_rotation")
			.style(ChatFormatting.DARK_GRAY)
			.forGoggles(tooltip);
		else {
			StressGaugeTooltipBehaviour.getFormattedStressText(stressFraction).forGoggles(tooltip);
			CreateLang.translate("gui.stressometer.capacity").style(ChatFormatting.GRAY).forGoggles(tooltip);
			double remainingCapacity = capacity - stress;
			var su = CreateLang.translate("generic.unit.stress");
			var stressTip = CreateLang.number(remainingCapacity).add(su).style(StressImpact.of(stressFraction).getRelativeColor());
			if (remainingCapacity != capacity)
				stressTip.text(ChatFormatting.GRAY, " / ").add(CreateLang.number(capacity).add(su).style(ChatFormatting.DARK_GRAY));
			stressTip.forGoggles(tooltip, 1);
		}
	}
	@Shadow
	protected abstract void addStressImpactStats(List<Component> tooltip, float stressAtBase);
}
