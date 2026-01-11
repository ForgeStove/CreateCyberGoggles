package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.client.AllBlockEntityBehaviours;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import io.github.forgestove.create_cyber_goggles.core.tooltip.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.zurrtum.create.client.AllBlockEntityBehaviours.add;
@Mixin(AllBlockEntityBehaviours.class)
public abstract class AllBlockEntityBehavioursMixin {
	@Inject(method = "register", at = @At("TAIL"))
	private static void register(CallbackInfo callbackInfo) {
		add(AllBlockEntityTypes.BRACKETED_KINETIC, KineticTooltipBehaviour::new);
		add(AllBlockEntityTypes.ENCASED_FAN, EncasedFanTooltipBehavior::new);
		add(AllBlockEntityTypes.NOZZLE, NozzleTooltipBehavior::new);
		add(AllBlockEntityTypes.HEATER, BlazeBurnerTooltipBehavior::new);
		add(AllBlockEntityTypes.SCHEMATICANNON, SchematicannonTooltipBehavior::new);
		add(AllBlockEntityTypes.BACKTANK, BacktankBlockEntityTooltipBehavior::new);
		add(AllBlockEntityTypes.BELT, BeltBlockEntityTooltipBehavior::new);
		add(AllBlockEntityTypes.PULSE_REPEATER, BrassDiodeBlockEntityTooltipBehavior::new);
		add(AllBlockEntityTypes.PULSE_EXTENDER, BrassDiodeBlockEntityTooltipBehavior::new);
		add(AllBlockEntityTypes.PULSE_TIMER, BrassDiodeBlockEntityTooltipBehavior::new);
	}
}
