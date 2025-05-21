package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.content.config.*;
import com.forgestove.create_cyber_goggles.content.util.Common;
import com.simibubi.create.content.kinetics.base.IRotate.SpeedLevel;
import com.simibubi.create.content.kinetics.base.*;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.*;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
public class KineticParticle {
	public static void tick(RenderLevelStageEvent event) {
		if (!CCGConfig.config.goggles.enableKineticEffect) return;
		if (event.getStage() != Stage.AFTER_PARTICLES) return;
		var kbe = Common.getSelectedKBE();
		if (kbe == null) return;
		var mc = Minecraft.getInstance();
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
			rotationAxis
		);
		mc.level.addParticle(particleData, center.x, center.y, center.z, 100, 0, 0);
	}
}
