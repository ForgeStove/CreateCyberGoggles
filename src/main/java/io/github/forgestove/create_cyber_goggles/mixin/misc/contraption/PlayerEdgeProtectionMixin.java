package io.github.forgestove.create_cyber_goggles.mixin.misc.contraption;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.foundation.util.*;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;
import org.joml.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.Math;
@Mixin(Player.class)
public abstract class PlayerEdgeProtectionMixin implements Self<Player> {
	@Shadow
	protected abstract boolean isAboveGround(float maxUpStep);
	@Inject(method = "maybeBackOffFromEdge", at = @At("HEAD"), cancellable = true)
	private void contraptionAwareBackOff(Vec3 movement, MoverType moverType, CallbackInfoReturnable<Vec3> cir) {
		if (!CCG.config.misc.allowSneakWalkOnContraption) return;
		if (!ContraptionRotationTracker.isOnContraption()) return;
		var player = thiz();
		if (!player.isShiftKeyDown() || player.getAbilities().flying) return;
		if (movement.y > 0) return;
		if (moverType != MoverType.SELF && moverType != MoverType.PLAYER) return;
		float maxUpStep = player.maxUpStep();
		if (!isAboveGround(maxUpStep)) return;
		var frame = new Quaternionf().rotateY(ContraptionRotationTracker.frameYaw());
		var local = frame.transformInverse(new Vector3f((float) movement.x, 0, (float) movement.z));
		double x = local.x;
		double z = local.z;
		var step = 0.05;
		double xStep = Math.signum(x) * step;
		double zStep = Math.signum(z) * step;
		while (x != 0 && ContraptionRotationTracker.wouldSlideOff(player, frame, x, 0, maxUpStep)) {
			if (Math.abs(x) <= step) {
				x = 0;
				break;
			}
			x -= xStep;
		}
		while (z != 0 && ContraptionRotationTracker.wouldSlideOff(player, frame, 0, z, maxUpStep)) {
			if (Math.abs(z) <= step) {
				z = 0;
				break;
			}
			z -= zStep;
		}
		while (x != 0 && z != 0 && ContraptionRotationTracker.wouldSlideOff(player, frame, x, z, maxUpStep)) {
			if (Math.abs(x) <= step) x = 0;
			else x -= xStep;
			if (Math.abs(z) <= step) z = 0;
			else z -= zStep;
		}
		var global = frame.transform(new Vector3f((float) x, 0, (float) z));
		cir.setReturnValue(new Vec3(global.x, movement.y, global.z));
	}
	@Inject(method = "canFallAtLeast", at = @At("RETURN"), cancellable = true)
	private void contraptionAwareEdgeProtection(double x, double z, float distance, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ()) return;
		if (!CCG.config.misc.allowSneakWalkOnContraption) return;
		var player = thiz();
		if (!player.isShiftKeyDown()) return;
		var bounds = player.getBoundingBox();
		var probe = new AABB(
			bounds.minX + x,
			bounds.minY - distance - 1.0E-5,
			bounds.minZ + z,
			bounds.maxX + x,
			bounds.minY,
			bounds.maxZ + z
		);
		if (ContraptionRotationTracker.hasSupport(player, probe)) cir.setReturnValue(false);
	}
}
