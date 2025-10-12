package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.kinetics.base.IRotate.SpeedLevel;
import com.simibubi.create.content.kinetics.base.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.RotationPropagatorAccessor;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import org.jetbrains.annotations.NotNull;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class KineticParticle {
	public static void tick(Post ignoredEvent) {
		if (!CCG.CONFIG.goggles.enableKineticEffect || mc.level == null || isInGUI()) return;
		var kbe = getBlockEntity(KineticBlockEntity.class);
		if (kbe == null) return;
		var kbeSpeed = kbe.getSpeed();
		if (kbeSpeed == 0) return;
		var state = kbe.getBlockState();
		if (!(state.getBlock() instanceof KineticBlock kb)) return;
		var center = VecHelper.getCenterOf(kbe.getBlockPos());
		var speedLevel = SpeedLevel.of(kbeSpeed);
		var particleSpeed = Math.max(15, speedLevel.getParticleSpeed()) * Math.signum(kbeSpeed);
		if (renderShaftParticles(kbe, kb, state, center, speedLevel.getColor(), particleSpeed)) return;
		renderDefaultParticles(kb, state, center, speedLevel.getColor(), particleSpeed);
	}
	private static boolean renderShaftParticles(
		KineticBlockEntity kbe,
		KineticBlock kb,
		BlockState state,
		Vec3 center,
		int color,
		float particleSpeed
	) {
		var hasRendered = false;
		for (var direction : Direction.values()) {
			if (!kb.hasShaftTowards(mc.level, kbe.getBlockPos(), state, direction)) continue;
			var axis = direction.getAxis();
			var directionSpeed = particleSpeed * RotationPropagatorAccessor.getAxisModifier(kbe, direction);
			var offset = 0.5 * direction.getAxisDirection().getStep();
			var axisVec = new Vec3(axis == Axis.X ? offset : 0, axis == Axis.Y ? offset : 0, axis == Axis.Z ? offset : 0);
			var pos = center.add(axisVec);
			var initial = Math.max(0.3f, kb.getParticleInitialRadius() / 2);
			var target = Math.max(0.3f, kb.getParticleTargetRadius() / 2);
			var particleData = new RotationIndicatorParticleData(color, directionSpeed, initial, target, 10, axis);
			spawnParticles(particleData, pos);
			hasRendered = true;
		}
		//noinspection ConstantValue
		return hasRendered;
	}
	private static void renderDefaultParticles(@NotNull KineticBlock kb, BlockState state, Vec3 center, int color, float particleSpeed) {
		var initial = kb.getParticleInitialRadius();
		var target = kb.getParticleTargetRadius();
		var particleData = new RotationIndicatorParticleData(color, particleSpeed, initial, target, 10, kb.getRotationAxis(state));
		spawnParticles(particleData, center);
	}
	private static void spawnParticles(RotationIndicatorParticleData particleData, Vec3 pos) {
		if (mc.level == null) return;
		for (var i = 0; i < 3; i++) mc.level.addParticle(particleData, pos.x, pos.y, pos.z, 0, 0, 0);
	}
}
