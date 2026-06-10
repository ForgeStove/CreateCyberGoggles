package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.zurrtum.create.client.content.trains.TrainHUD;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(TrainHUD.class)
public abstract class TrainHUDMixin {
	@ModifyConstant(method = "onScroll", constant = @Constant(doubleValue = (double) (1 / 18F)))
	private static double onScroll(double minSpeed) {
		return CCG.config.misc.enableNegativeInfThrottle ? Float.NEGATIVE_INFINITY : minSpeed;
	}
}
