package io.github.forgestove.create_cyber_goggles.mixin.simulated;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffItem;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.isServer;
@Mixin(PhysicsStaffItem.class)
public abstract class PhysicsStaffItemMixin {
	@Inject(method = "isHolding", at = @At("HEAD"), cancellable = true)
	private static void isHolding(Player player, CallbackInfoReturnable<Boolean> cir) {
		if (isServer()) return;
		if (!CCGKey.usePhysicsStaff.isDown()) return;
		cir.setReturnValue(true);
	}
}
