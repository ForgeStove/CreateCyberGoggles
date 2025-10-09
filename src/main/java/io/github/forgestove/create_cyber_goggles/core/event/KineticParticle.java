package io.github.forgestove.create_cyber_goggles.core.event;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.IRotate.SpeedLevel;
import com.zurrtum.create.content.kinetics.base.*;
import com.zurrtum.create.infrastructure.particle.RotationIndicatorParticleData;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class KineticParticle {
	public static void tick(Minecraft mc) {
		if (!CCG.CONFIG.goggles.enableKineticEffect) return;
		if (mc.level == null || isInGUI()) return;
		var kbe = getBlockEntity(KineticBlockEntity.class);
		if (kbe == null) return;
		var speed = kbe.getSpeed();
		if (speed == 0) return;
		var state = kbe.getBlockState();
		if (!(state.getBlock() instanceof KineticBlock kb)) return;
		var axis = kb.getRotationAxis(state);
		if (axis == null) return;
		var center = VecHelper.getCenterOf(kbe.getBlockPos());
		var speedLevel = SpeedLevel.of(speed);
		var particleData = new RotationIndicatorParticleData(
			speedLevel.getColor(),
			Math.max(15, speedLevel.getParticleSpeed()) * Math.signum(speed),
			kb.getParticleInitialRadius() / 2,
			kb.getParticleTargetRadius() / 2,
			10,
			axis
		);
		var offset = 0.5;
		var axisVec = new Vec3(axis == Axis.X ? offset : 0, axis == Axis.Y ? offset : 0, axis == Axis.Z ? offset : 0);
		for (var i = -1; i <= 1; i += 2) {
			var pos = center.add(axisVec.scale(i));
			for (var j = 0; j < 3; j++) mc.level.addParticle(particleData, pos.x, pos.y, pos.z, 0, 0, 0);
		}
	}
}
