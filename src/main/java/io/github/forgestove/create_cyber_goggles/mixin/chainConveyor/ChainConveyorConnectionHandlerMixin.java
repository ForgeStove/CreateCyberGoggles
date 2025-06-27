package io.github.forgestove.create_cyber_goggles.mixin.chainConveyor;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorConnectionHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.core.*;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(value = ChainConveyorConnectionHandler.class, remap = false)
public abstract class ChainConveyorConnectionHandlerMixin {
	@WrapOperation(
		method = "validateAndConnect",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;closerThan(Lnet/minecraft/core/Vec3i;D)Z", ordinal = 1)
	)
	private static boolean wrapCloserThan(BlockPos instance, Vec3i vec3i, double distance, Operation<Boolean> original) {
		return !CCG.CONFIG.chainConveyor.enhancedConnection && original.call(instance, vec3i, distance);
	}
	@WrapOperation(
		method = "validateAndConnect", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/world/phys/Vec3;atLowerCornerOf(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/world/phys/Vec3;"
	)
	)
	private static Vec3 wrapDiff(Vec3i vec3i, Operation<Vec3> original) {
		return CCG.CONFIG.chainConveyor.enhancedConnection ? new Vec3(2, 0, 2) : original.call(vec3i);
	}
}
