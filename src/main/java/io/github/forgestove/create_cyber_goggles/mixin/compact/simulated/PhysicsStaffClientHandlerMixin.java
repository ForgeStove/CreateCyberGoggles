package io.github.forgestove.create_cyber_goggles.mixin.compact.simulated;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffClientHandler;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffClientHandler.ClientDragSession;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Pseudo
@Mixin(PhysicsStaffClientHandler.class)
public abstract class PhysicsStaffClientHandlerMixin {
	@Shadow @Nullable private ClientDragSession dragSession;
	@Inject(method = "tick", at = @At("TAIL"))
	private void tick(CallbackInfo ci) {
		if (dragSession == null) return;
		if (mc.player == null) return;
		if (!CCGKey.correctionSublevel.isDown()) return;
		var subLevel = dragSession.dragSubLevel();
		var localAnchor = dragSession.dragLocalAnchor();
		dragSession = new ClientDragSession(
			subLevel,
			localAnchor,
			new Quaterniond(),
			clampDistance(mc.player.getEyePosition()
				.distanceTo(subLevel.logicalPose().transformPosition(JOMLConversion.toMojang(localAnchor))))
		);
	}
	@Shadow
	protected abstract double clampDistance(double distance);
}
