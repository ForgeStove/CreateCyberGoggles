package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.TooltipUtil;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(KineticBlockEntity.class)
public abstract class KineticBlockEntityMixin {
	@Shadow protected float capacity, stress;
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
	public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> returnable) {
		var goggles = CCG.CONFIG.goggles;
		if (!goggles.enhancedInfo) return;
		var kbe = (KineticBlockEntity) (Object) this;
		var speed = kbe.getTheoreticalSpeed();
		var hide = !goggles.hideStaticKineticInfo || speed != 0;
		returnable.setReturnValue(hide);
		if (!hide) return;
		TooltipUtil.kinetic(tooltip, kbe, stress, capacity);
	}
}
