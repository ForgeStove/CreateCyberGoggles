package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.content.kinetics.base.IRotate.*;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Lang;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.event.CCGKey;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(value = KineticBlockEntity.class, remap = false)
public abstract class KineticBlockEntityMixin {
	@Shadow protected float capacity, stress, speed;
	@Shadow protected boolean overStressed;
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
	public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> returnable) {
		var goggles = CCG.CONFIG.goggles;
		if (!goggles.enhancedInfo) return;
		var hide = !goggles.hideStaticKineticInfo || !Mth.equal(speed, 0);
		returnable.setReturnValue(hide);
		if (!hide) return;
		if (StressImpact.isEnabled()) {
			var stressAtBase = calculateStressApplied();
			if (!Mth.equal(stressAtBase, 0)) addStressImpactStats(tooltip, stressAtBase);
		}
		Lang.translate("gui.goggles.kinetic_stats").forGoggles(tooltip);
		SpeedLevel.getFormattedSpeedText(speed, overStressed).forGoggles(tooltip);
		if (!CCGKey.showStress.isKeyDown()) return;
		double stressFraction = stress / (capacity == 0 ? 1 : capacity);
		Lang.translate("gui.stressometer.title").style(ChatFormatting.GRAY).forGoggles(tooltip);
		if (speed == 0) Lang.text(TooltipHelper.makeProgressBar(3, 0))
			.translate("gui.stressometer.no_rotation")
			.style(ChatFormatting.DARK_GRAY)
			.forGoggles(tooltip);
		else {
			StressImpact.getFormattedStressText(stressFraction).forGoggles(tooltip);
			Lang.translate("gui.stressometer.capacity").style(ChatFormatting.GRAY).forGoggles(tooltip);
			double remainingCapacity = capacity - stress;
			var su = Lang.translate("generic.unit.stress");
			var stressTip = Lang.number(remainingCapacity).add(su).style(StressImpact.of(stressFraction).getRelativeColor());
			if (remainingCapacity != capacity)
				stressTip.text(ChatFormatting.GRAY, " / ").add(Lang.number(capacity).add(su).style(ChatFormatting.DARK_GRAY));
			stressTip.forGoggles(tooltip, 1);
		}
	}
	@Shadow
	public abstract float calculateStressApplied();
	@Shadow
	protected abstract void addStressImpactStats(List<Component> tooltip, float stressAtBase);
}
