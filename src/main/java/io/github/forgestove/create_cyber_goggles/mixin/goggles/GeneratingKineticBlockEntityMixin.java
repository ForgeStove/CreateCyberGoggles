package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.*;
import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(GeneratingKineticTooltipBehaviour.class)
public abstract class GeneratingKineticBlockEntityMixin extends KineticTooltipBehaviour<GeneratingKineticBlockEntity> {
	public GeneratingKineticBlockEntityMixin(GeneratingKineticBlockEntity be) {
		super(be);
	}
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
	public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
		var goggles = CCG.config.goggles;
		if (!goggles.enhancedInfo) return;
		var speed = blockEntity.getTheoreticalSpeed();
		if (goggles.hideStaticKineticInfo && speed == 0) {
			cir.setReturnValue(false);
			return;
		}
		GoggleTooltipUtil.generatingKinetic(tooltip, blockEntity);
		cir.setReturnValue(super.addToGoggleTooltip(tooltip, isPlayerSneaking));
	}
}
