package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.GoggleTooltip;
import io.github.forgestove.create_cyber_goggles.content.factoryPanel.FluidTankThroughput;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.data.FluidTankThroughputData;
import io.github.forgestove.create_cyber_goggles.foundation.util.Self;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.List;
@Mixin(FluidTankBlockEntity.class)
public abstract class FluidTankBlockEntityMixin implements FluidTankThroughputData, Self<FluidTankBlockEntity> {
	@Unique private final FluidTankThroughput ccg$throughput = new FluidTankThroughput();
	@Inject(method = "tick", at = @At("TAIL"))
	private void ccg$tick(CallbackInfo ci) {
		var level = thiz().getLevel();
		if (level == null || !level.isClientSide || !thiz().isController()) return;
		ccg$throughput.tick(thiz().getTank(0).getFluidAmount(), CCG.config.goggles.fluidThroughputWindow);
	}
	@Inject(method = "addToGoggleTooltip", at = @At("RETURN"), cancellable = true)
	private void ccg$addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(GoggleTooltip.dispatch(tooltip, thiz(), isPlayerSneaking, null) || cir.getReturnValue());
	}
	@Override
	public double ccg$getRate() {
		return ccg$throughput.rate();
	}
}
