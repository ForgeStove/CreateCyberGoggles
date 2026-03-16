package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(KineticBlockEntity.class)
public abstract class KineticBlockEntityMixin implements Self<KineticBlockEntity> {
	@Shadow protected float capacity, stress;
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
	public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
		var goggles = CCG.config.goggles;
		if (!goggles.enhancedInfo) return;
		var kbe = self();
		var speed = kbe.getTheoreticalSpeed();
		var hide = !goggles.hideStaticKineticInfo || speed != 0;
		cir.setReturnValue(hide);
		if (!hide) return;
		GoggleTooltipUtil.kinetic(tooltip, kbe, stress, capacity);
	}
}
