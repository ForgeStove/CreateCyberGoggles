package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.zurrtum.create.client.content.kinetics.mechanicalArm.ArmInteractionPointHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.core.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ArmInteractionPointHandler.class)
public class ArmInteractionPointHandlerMixin {
	@WrapOperation(
		method = "flushSettings", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;closerThan(Lnet/minecraft/core/Vec3i;D)Z"
	)
	)
	private static boolean flushSettings(BlockPos instance, Vec3i vector, double distance, Operation<Boolean> original) {
		return CCG.CONFIG.misc.removeMechanicalArmLimit || original.call(instance, vector, distance);
	}
}
