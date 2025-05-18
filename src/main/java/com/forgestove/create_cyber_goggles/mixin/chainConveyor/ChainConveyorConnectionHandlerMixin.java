package com.forgestove.create_cyber_goggles.mixin.chainConveyor;
import com.forgestove.create_cyber_goggles.content.config.*;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorConnectionHandler;
import net.minecraft.core.*;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(ChainConveyorConnectionHandler.class)
public abstract class ChainConveyorConnectionHandlerMixin {
	@Redirect(
		method = "validateAndConnect",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;closerThan(Lnet/minecraft/core/Vec3i;D)Z", ordinal = 1),
		remap = false
	)
	private static boolean redirectCloserThan(BlockPos instance, Vec3i vec3i, double distance) {
		return !CCGConfig.config.chainConveyor.enhancedConnection && instance.closerThan(vec3i, distance);
	}
	@Redirect(
		method = "validateAndConnect", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/world/phys/Vec3;atLowerCornerOf(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/world/phys/Vec3;"
	), remap = false
	)
	private static Vec3 redirectDiff(
		Vec3i vec3i
	) {
		return CCGConfig.config.chainConveyor.enhancedConnection ? new Vec3(2, 0, 2) : Vec3.atLowerCornerOf(vec3i);
	}
}
