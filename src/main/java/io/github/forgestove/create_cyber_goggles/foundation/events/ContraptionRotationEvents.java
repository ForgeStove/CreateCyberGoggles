package io.github.forgestove.create_cyber_goggles.foundation.events;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.foundation.util.ContraptionRotationTracker;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;

import static io.github.forgestove.create_cyber_goggles.foundation.util.CCGUtil.mc;
public class ContraptionRotationEvents {
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
		float yawDelta = -Mth.wrapDegrees((float) Math.toDegrees(2.0 * Math.atan2(delta.y, delta.w)));
		if (Math.abs(yawDelta) < 1.0E-3F) return;
		float newYaw = player.getYRot() + yawDelta;
		player.setYRot(newYaw);
		player.yHeadRot = newYaw;
		player.yBodyRot += yawDelta;
	}
}
