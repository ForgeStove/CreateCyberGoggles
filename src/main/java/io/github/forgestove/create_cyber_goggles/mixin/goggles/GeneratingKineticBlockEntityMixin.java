package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.*;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(value = GeneratingKineticTooltipBehaviour.class, remap = false)
public abstract class GeneratingKineticBlockEntityMixin<T extends KineticBlockEntity> extends KineticTooltipBehaviour<T> {
	public GeneratingKineticBlockEntityMixin(T be) {
		super(be);
	}
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
	public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> returnable) {
		var goggles = CCG.CONFIG.goggles;
		if (!goggles.enhancedInfo) return;
		var speed = blockEntity.getTheoreticalSpeed();
		if (goggles.hideStaticKineticInfo && speed == 0) {
			returnable.setReturnValue(false);
			return;
		}
		var stressBase = blockEntity.calculateAddedStressCapacity();
		if (!Mth.equal(stressBase, 0)) {
			CreateLang.translate("gui.goggles.generator_stats").forGoggles(tooltip);
			CreateLang.translate("tooltip.capacityProvided").style(ChatFormatting.GRAY).forGoggles(tooltip);
			if (speed != blockEntity.getGeneratedSpeed()) stressBase *= blockEntity.getGeneratedSpeed() / speed;
			CreateLang.number(Math.abs(stressBase * speed))
				.translate("generic.unit.stress")
				.style(ChatFormatting.AQUA)
				.space()
				.add(CreateLang.translate("gui.goggles.at_current_speed").style(ChatFormatting.DARK_GRAY))
				.forGoggles(tooltip);
		}
		returnable.setReturnValue(super.addToGoggleTooltip(tooltip, isPlayerSneaking));
	}
}
