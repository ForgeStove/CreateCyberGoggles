package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.*;
import com.zurrtum.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(PoweredShaftTooltipBehaviour.class)
public abstract class PoweredShaftBlockEntityMixin extends GeneratingKineticTooltipBehaviour<PoweredShaftBlockEntity> {
	public PoweredShaftBlockEntityMixin(PoweredShaftBlockEntity be) {
		super(be);
	}
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
	public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
		var goggles = CCG.config.goggles;
		if (!goggles.enhancedInfo || goggles.hideStaticKineticInfo) return;
		cir.setReturnValue(super.addToGoggleTooltip(tooltip, isPlayerSneaking));
	}
}
