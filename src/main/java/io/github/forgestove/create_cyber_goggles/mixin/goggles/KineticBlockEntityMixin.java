package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.CCGConfig.Goggles;
import io.github.forgestove.create_cyber_goggles.api.SelfKineticInfo;
import io.github.forgestove.create_cyber_goggles.util.GoggleTooltipUtil;
import io.github.forgestove.create_cyber_goggles.util.contract.Self;
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
		Goggles goggles = CCG.config.goggles;
		if (!goggles.enhancedInfo) return;
		KineticBlockEntity kbe = thiz();
		if (kbe instanceof SelfKineticInfo) return;
		float speed = kbe.getTheoreticalSpeed();
		boolean hide = !goggles.hideStaticKineticInfo || speed != 0;
		cir.setReturnValue(hide);
		if (!hide) return;
		GoggleTooltipUtil.kinetic(tooltip, kbe, stress, capacity);
	}
}
