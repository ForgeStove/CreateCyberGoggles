package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.forgestove.create_cyber_goggles.content.util.Common;
import com.simibubi.create.content.kinetics.base.IRotate.SpeedLevel;
import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.foundation.utility.VecHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
public class KineticEffector {
	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(KineticEffector::tick);
	}
	public static void tick(Minecraft mc) {
		if (!CCGConfig.config.goggles.enableKineticEffect) return;
		var kbe = Common.getSelectedKBE();
		if (kbe == null) return;
		if (mc.level == null) return;
		var speed = kbe.getSpeed();
		if (speed == 0) return;
		var state = kbe.getBlockState();
		if (!(state.getBlock() instanceof KineticBlock kb)) return;
		var rotationAxis = kb.getRotationAxis(state);
		if (rotationAxis == null) return;
		var center = VecHelper.getCenterOf(kbe.getBlockPos());
		var speedLevel = SpeedLevel.of(speed);
		var particleData = new RotationIndicatorParticleData(
			speedLevel.getColor(),
			Math.max(15, speedLevel.getParticleSpeed()) * Math.signum(speed),
			kb.getParticleInitialRadius(),
			kb.getParticleTargetRadius(),
			20,
			rotationAxis.name().charAt(0)
		);
		mc.level.addParticle(particleData, center.x, center.y, center.z, 0, 0, 0);
	}
}
