package io.github.forgestove.create_cyber_goggles.mixin.tooltip;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.*;
import com.zurrtum.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(HosePulleyTooltipBehaviour.class)
public abstract class HosePulleyTooltipBehaviourMixin extends KineticTooltipBehaviour<HosePulleyBlockEntity> {
	public HosePulleyTooltipBehaviourMixin(HosePulleyBlockEntity be) {
		super(be);
	}
	@Inject(method = "addToGoggleTooltip", at = @At("RETURN"), cancellable = true)
	public void addToGoggleTooltip(
		List<Component> tooltip,
		boolean isPlayerSneaking,
		CallbackInfoReturnable<Boolean> cir,
		@Local(name = "addToGoggleTooltip") boolean addToGoggleTooltip
	) {
		CCGLang.fluidEntry(blockEntity.handler.getStack(0), blockEntity.handler.getMaxAmountPerStack()).forGoggles(tooltip);
		cir.setReturnValue(addToGoggleTooltip || blockEntity.infinite);
	}
}
