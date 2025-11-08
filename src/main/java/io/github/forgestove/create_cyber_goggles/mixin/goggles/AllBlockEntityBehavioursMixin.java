package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.zurrtum.create.client.AllBlockEntityBehaviours;
import com.zurrtum.create.content.equipment.armor.BacktankBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.fan.*;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.zurrtum.create.content.schematics.cannon.SchematicannonBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.tooltip.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.zurrtum.create.client.AllBlockEntityBehaviours.add;
@Mixin(value = AllBlockEntityBehaviours.class, remap = false)
public abstract class AllBlockEntityBehavioursMixin {
	@Inject(method = "register", at = @At("TAIL"))
	private static void register(CallbackInfo callbackInfo) {
		add(EncasedFanBlockEntity.class, EncasedFanTooltipBehavior::new);
		add(NozzleBlockEntity.class, NozzleTooltipBehavior::new);
		add(BlazeBurnerBlockEntity.class, BlazeBurnerTooltipBehavior::new);
		add(SchematicannonBlockEntity.class, SchematicannonTooltipBehavior::new);
		add(BacktankBlockEntity.class, BacktankBlockEntityTooltipBehavior::new);
		add(BeltBlockEntity.class, BeltBlockEntityTooltipBehavior::new);
	}
}
