package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.kinetics.base.IRotate.SpeedLevel;
import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.RotationPropagatorAccessor;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import org.jetbrains.annotations.NotNull;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public final class KineticParticle {
	public static void tick(Post ignoredEvent) {
		if (!CCG.config.goggles.enableKineticEffect || mc.level == null || isInGUI()) return;
		KineticBlockEntity kbe = getBlockEntity(KineticBlockEntity.class);
		if (kbe == null) return;
		float kbeSpeed = kbe.getSpeed();
		if (kbeSpeed == 0) return;
		BlockState state = kbe.getBlockState();
		if (!(state.getBlock() instanceof KineticBlock kb)) return;
		Vec3 center = VecHelper.getCenterOf(kbe.getBlockPos());
		SpeedLevel speedLevel = SpeedLevel.of(kbeSpeed);
		float particleSpeed = Math.max(10, speedLevel.getParticleSpeed()) * Math.signum(kbeSpeed);
		if (renderShaftParticles(kbe, kb, state, center, speedLevel.getColor(), particleSpeed)) return;
		if (kb instanceof BeltBlock) return;
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
		for (Direction direction : Direction.values()) {
			if (!kb.hasShaftTowards(mc.level, kbe.getBlockPos(), state, direction)) continue;
			Axis axis = direction.getAxis();
			float directionSpeed = particleSpeed * RotationPropagatorAccessor.callGetAxisModifier(kbe, direction);
			double offset = direction.getAxisDirection().getStep() / 2D;
			var axisVec = new Vec3(axis == Axis.X ? offset : 0, axis == Axis.Y ? offset : 0, axis == Axis.Z ? offset : 0);
			Vec3 pos = center.add(axisVec);
			float initial = Mth.clamp(kb.getParticleInitialRadius() / 2, 0.2F, 0.3F);
			float target = Mth.clamp(kb.getParticleTargetRadius() / 2, 0.4F, 0.5F);
			var particleData = new RotationIndicatorParticleData(color, directionSpeed, initial, target, 10, axis);
			spawnParticles(particleData, pos);
			hasRendered = true;
		}
		if (kb instanceof EncasedCogwheelBlock) return false;
		//noinspection ConstantValue
		return hasRendered;
	}
	private static void renderDefaultParticles(@NotNull KineticBlock kb, BlockState state, Vec3 center, int color, float particleSpeed) {
		float initial = kb.getParticleInitialRadius();
		float target = kb.getParticleTargetRadius();
		var particleData = new RotationIndicatorParticleData(color, particleSpeed, initial, target, 10, kb.getRotationAxis(state));
		spawnParticles(particleData, center);
	}
	private static void spawnParticles(RotationIndicatorParticleData particleData, Vec3 pos) {
		if (mc.level == null) return;
		for (var i = 0; i < 3; i++) mc.level.addParticle(particleData, pos.x, pos.y, pos.z, 0, 0, 0);
	}
}
