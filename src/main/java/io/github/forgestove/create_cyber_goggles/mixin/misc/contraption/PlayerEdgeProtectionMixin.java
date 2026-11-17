package io.github.forgestove.create_cyber_goggles.mixin.misc.contraption;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.foundation.util.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Player.class)
public abstract class PlayerEdgeProtectionMixin implements Self<Player> {
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
