package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.kinetics.base.IRotate.SpeedLevel;
import com.simibubi.create.content.kinetics.base.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.RotationPropagatorAccessor;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class KineticParticle {
	public static void tick(Post ignoredEvent) {
		if (!CCG.CONFIG.goggles.enableKineticEffect) return;
		if (mc.level == null || isInGUI()) return;
		var kbe = getBlockEntity(KineticBlockEntity.class);
		if (kbe == null) return;
		var kbeSpeed = kbe.getSpeed();
		if (kbeSpeed == 0) return;
		var state = kbe.getBlockState();
		if (!(state.getBlock() instanceof KineticBlock kb)) return;
		var center = VecHelper.getCenterOf(kbe.getBlockPos());
		var speedLevel = SpeedLevel.of(kbeSpeed);
		var color = speedLevel.getColor();
		var particleSpeed = Math.max(15, speedLevel.getParticleSpeed()) * Math.signum(kbeSpeed);
		var radiusInitial = Math.max(0.3f, kb.getParticleInitialRadius() / 2);
		var radiusTarget = Math.max(0.3f, kb.getParticleTargetRadius() / 2);
		for (var direction : Direction.values()) {
			if (!kb.hasShaftTowards(mc.level, kbe.getBlockPos(), state, direction)) continue;
			var axis = direction.getAxis();
			var directionSpeed = particleSpeed * RotationPropagatorAccessor.getAxisModifier(kbe, direction);
			var particleData = new RotationIndicatorParticleData(color, directionSpeed, radiusInitial, radiusTarget, 10, axis);
			var offset = 0.5 * direction.getAxisDirection().getStep();
			var axisVec = new Vec3(axis == Axis.X ? offset : 0, axis == Axis.Y ? offset : 0, axis == Axis.Z ? offset : 0);
			var pos = center.add(axisVec);
			for (var j = 0; j < 3; j++) mc.level.addParticle(particleData, pos.x, pos.y, pos.z, 0, 0, 0);
		}
	}
}
