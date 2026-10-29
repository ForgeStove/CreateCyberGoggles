package io.github.forgestove.create_cyber_goggles.mixin.misc.contraption;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.foundation.collision.ContinuousOBBCollider;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ContinuousOBBCollider.class)
public abstract class ContinuousOBBColliderMixin {
	@ModifyExpressionValue(
		method = "collideMany", at = @At(
		value = "FIELD",
		target = "Lcom/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold;"
			+ "axis:Lnet/minecraft/world/phys/Vec3;",
		opcode = Opcodes.GETFIELD
	)
	)
	private static Vec3 keepAxisSafe(Vec3 original) {
		return original != null ? original : Vec3.ZERO;
	}
	@ModifyExpressionValue(
		method = "collideMany", at = @At(
		value = "FIELD",
		target = "Lcom/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold;"
			+ "normalAxis:Lnet/minecraft/world/phys/Vec3;",
		opcode = Opcodes.GETFIELD
	)
	)
	private static Vec3 keepNormalAxisSafe(Vec3 original) {
		return original != null ? original : Vec3.ZERO;
	}
}
