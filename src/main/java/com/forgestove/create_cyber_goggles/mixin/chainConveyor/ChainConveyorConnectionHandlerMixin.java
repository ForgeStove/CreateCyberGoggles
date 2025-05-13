package com.forgestove.create_cyber_goggles.mixin.chainConveyor;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorConnectionHandler;
import net.minecraft.core.*;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(ChainConveyorConnectionHandler.class)
public abstract class ChainConveyorConnectionHandlerMixin {
	@Redirect(
		method = "validateAndConnect",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;closerThan(Lnet/minecraft/core/Vec3i;D)Z", ordinal = 1)
	)
	private static boolean redirectCloserThan(BlockPos instance, Vec3i vec3i, double distance) {
		if (CCGConfig.get().chainConveyor.enhancedConnection) return false;
		return instance.closerThan(vec3i, distance);
	}
	@Redirect(
		method = "validateAndConnect", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/world/phys/Vec3;atLowerCornerOf(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/world/phys/Vec3;"
	)
	)
	private static Vec3 redirectDiff(
		Vec3i vec3i
	) {
		if (CCGConfig.get().chainConveyor.enhancedConnection) return new Vec3(2, 0, 2);
		return Vec3.atLowerCornerOf(vec3i);
	}
}
