package io.github.forgestove.create_cyber_goggles.mixin.goggles.provider;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import io.github.forgestove.create_cyber_goggles.api.GoggleTooltip;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.data.FluidTankDeltaData;
import io.github.forgestove.create_cyber_goggles.foundation.util.Self;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;
@Mixin(FluidTankBlockEntity.class)
public abstract class FluidTankBlockEntityMixin implements FluidTankDeltaData, Self<FluidTankBlockEntity> {
	@Unique private static final int CCG_DELTA_WINDOW = 100;
	@Unique private final Deque<Integer> ccg$deltas = new ArrayDeque<>();
	@Unique private int ccg$lastAmount = -1;
	@Inject(method = "tick", at = @At("TAIL"))
	private void ccg$tick(CallbackInfo ci) {
		var level = thiz().getLevel();
		if (level == null || !level.isClientSide || !thiz().isController()) return;
		var amount = thiz().getTank(0).getFluidAmount();
		if (ccg$lastAmount >= 0) {
			ccg$deltas.addLast(amount - ccg$lastAmount);
			while (ccg$deltas.size() > CCG_DELTA_WINDOW) ccg$deltas.pollFirst();
		}
		ccg$lastAmount = amount;
	}
	@Override
	public double ccg$getDelta() {
		return ccg$deltas.stream().mapToInt(Integer::intValue).average().orElse(0);
	}
	@Inject(method = "addToGoggleTooltip", at = @At("RETURN"), cancellable = true)
	private void ccg$addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(GoggleTooltip.dispatch(tooltip, thiz(), isPlayerSneaking, null) || cir.getReturnValue());
	}
}
