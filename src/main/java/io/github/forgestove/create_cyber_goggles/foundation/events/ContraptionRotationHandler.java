package io.github.forgestove.create_cyber_goggles.foundation.events;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.foundation.util.ContraptionRotationTracker;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import org.joml.Vector3f;

import static io.github.forgestove.create_cyber_goggles.foundation.util.CCGUtil.mc;
public class ContraptionRotationHandler {
	public static void onClientTick(Post ignoredEvent) {
		var player = mc.player;
		if (player == null) return;
		ContraptionRotationTracker.refreshPose(player);
		if (!CCG.config.misc.followContraptionRotation) {
			ContraptionRotationTracker.reset();
			return;
		}
		var delta = ContraptionRotationTracker.delta();
		if (delta == null) return;
		var forward = Vec3.directionFromRotation(0, player.getYRot());
		var rotated = delta.transform(new Vector3f((float) forward.x, (float) forward.y, (float) forward.z));
		float horizontalSqr = rotated.x * rotated.x + rotated.z * rotated.z;
		if (horizontalSqr < 1.0E-6F) return;
		var yaw = (float) Math.toDegrees(Math.atan2(-rotated.x, rotated.z));
		float yawDelta = Mth.wrapDegrees(yaw - player.getYRot());
		float newYaw = player.getYRot() + yawDelta;
		player.setYRot(newYaw);
		player.yHeadRot = newYaw;
		player.yBodyRot += yawDelta;
	}
}
