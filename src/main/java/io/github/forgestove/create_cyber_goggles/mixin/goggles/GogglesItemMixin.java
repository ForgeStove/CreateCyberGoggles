package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Mixin(GogglesItem.class)
public abstract class GogglesItemMixin {
	@Inject(method = "isWearingGoggles", at = @At("HEAD"), cancellable = true)
	private static void isWearingGoggles(CallbackInfoReturnable<Boolean> cir) {
		if (mc.gameMode == null) return;
		var mode = CCG.config.gameMode;
		if (mode.enableGoggles && switch (mc.gameMode.getPlayerMode()) {
			case SURVIVAL -> mode.enableInSurvival;
			case CREATIVE -> mode.enableInCreative;
			case SPECTATOR -> mode.enableInSpectator;
			case ADVENTURE -> mode.enableInAdventure;
		}) cir.setReturnValue(true);
	}
}
