package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.*;
import com.zurrtum.create.content.kinetics.base.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(GeneratingKineticTooltipBehaviour.class)
public abstract class GeneratingKineticBlockEntityMixin<T extends KineticBlockEntity> extends KineticTooltipBehaviour<T> {
	public GeneratingKineticBlockEntityMixin(T be) {
		super(be);
	}
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
	public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
		var goggles = CCG.CONFIG.goggles;
		if (!goggles.enhancedInfo) return;
		var speed = blockEntity.getTheoreticalSpeed();
		if (goggles.hideStaticKineticInfo && speed == 0) {
			cir.setReturnValue(false);
			return;
		}
		GoggleTooltipUtil.generatingKinetic(tooltip, (GeneratingKineticBlockEntity) blockEntity);
		cir.setReturnValue(super.addToGoggleTooltip(tooltip, isPlayerSneaking));
	}
}
