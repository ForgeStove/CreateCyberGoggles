package io.github.forgestove.create_cyber_goggles.mixin.compact.simulated;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.simulated_team.simulated.content.blocks.handle.ClientHandleHandler;
import dev.simulated_team.simulated.util.hold_interaction.BlockHoldInteraction;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
@Pseudo
@Mixin(ClientHandleHandler.class)
public abstract class ClientHandleHandlerMixin extends BlockHoldInteraction {
	@Shadow private float desiredRange;
	@WrapMethod(method = "deltaRange")
	public void deltaRange(Player player, float delta, Operation<Void> original) {
		if (!CCG.config.aeronautics.liftLimitOfHandleRange) {
			original.call(player, delta);
			return;
		}
		desiredRange = desiredRange + delta;
	}
	@WrapOperation(method = "startHold", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(DD)D", ordinal = 0))
	public double wrapHandleRange(double a, double b, Operation<Double> original) {
		if (!CCG.config.aeronautics.liftLimitOfHandleRange) return original.call(a, b);
		return a;
	}
	@WrapOperation(
		method = "startHold", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isShiftKeyDown()Z"
	)
	)
	public boolean wrapShiftKeyDown(Player instance, Operation<Boolean> original) {
		if (!CCG.config.aeronautics.customHandleMoveSublevelKey) return original.call(instance);
		return CCGKey.handleMoveSublevel.isDown();
	}
}
