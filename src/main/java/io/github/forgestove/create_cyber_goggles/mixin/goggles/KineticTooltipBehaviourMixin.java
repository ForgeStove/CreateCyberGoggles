package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.*;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.KineticBlockEntityAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(KineticTooltipBehaviour.class)
public abstract class KineticTooltipBehaviourMixin<T extends KineticBlockEntity> extends TooltipBehaviour<T> {
	public KineticTooltipBehaviourMixin(T be) {
		super(be);
	}
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
	public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> returnable) {
		var goggles = CCG.config.goggles;
		if (!goggles.enhancedInfo) return;
		var hide = !goggles.hideStaticKineticInfo || !Mth.equal(blockEntity.getTheoreticalSpeed(), 0);
		returnable.setReturnValue(hide);
		if (!hide) return;
		var accessor = (KineticBlockEntityAccessor) blockEntity;
		GoggleTooltipUtil.kinetic(tooltip, blockEntity, accessor.getStress(), accessor.getCapacity());
	}
}
