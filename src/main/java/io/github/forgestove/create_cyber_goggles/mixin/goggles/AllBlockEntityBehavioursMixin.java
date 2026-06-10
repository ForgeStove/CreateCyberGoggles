package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.zurrtum.create.client.AllBlockEntityBehaviours;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import io.github.forgestove.create_cyber_goggles.core.tooltip.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.zurrtum.create.AllBlockEntityTypes.*;
import static com.zurrtum.create.client.AllBlockEntityBehaviours.add;
@Mixin(AllBlockEntityBehaviours.class)
public abstract class AllBlockEntityBehavioursMixin {
	@Inject(method = "register", at = @At("TAIL"))
	private static void register(CallbackInfo callbackInfo) {
		add(BRACKETED_KINETIC, KineticTooltipBehaviour::new);
		add(ENCASED_FAN, EncasedFanTooltipBehavior::new);
		add(NOZZLE, NozzleTooltipBehavior::new);
		add(HEATER, BlazeBurnerTooltipBehavior::new);
		add(SCHEMATICANNON, SchematicannonTooltipBehavior::new);
		add(BACKTANK, BacktankTooltipBehavior::new);
		add(BELT, BeltTooltipBehavior::new);
		add(PULSE_REPEATER, BrassDiodeTooltipBehavior::new);
		add(PULSE_EXTENDER, BrassDiodeTooltipBehavior::new);
		add(PULSE_TIMER, BrassDiodeTooltipBehavior::new);
		add(DEPOT, DepotTooltipBehavior::new);
		add(CRUSHING_WHEEL_CONTROLLER, CrushingWheelControllerTooltipBehavior::new);
		add(MILLSTONE, MillstoneTooltipBehavior::new);
	}
}
