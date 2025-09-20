package io.github.forgestove.create_cyber_goggles.event;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.IRotate.SpeedLevel;
import com.zurrtum.create.content.kinetics.base.KineticBlock;
import com.zurrtum.create.infrastructure.particle.RotationIndicatorParticleData;
import io.github.forgestove.create_cyber_goggles.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
public class KineticParticle {
	public static void tick(WorldRenderContext ignoredContext) {
		if (!CCG.CONFIG.goggles.enableKineticEffect) return;
		var kbe = Common.getKBE();
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
		mc.level.addParticle(particleData, center.x, center.y, center.z, 0, 0, 0);
	}
}
