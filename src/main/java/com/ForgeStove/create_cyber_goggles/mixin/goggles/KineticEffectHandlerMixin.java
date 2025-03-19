package com.ForgeStove.create_cyber_goggles.mixin.goggles;
import com.ForgeStove.create_cyber_goggles.Config;
import com.simibubi.create.content.kinetics.base.IRotate.SpeedLevel;
import com.simibubi.create.content.kinetics.base.*;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(KineticEffectHandler.class) public abstract class KineticEffectHandlerMixin {
	@Shadow KineticBlockEntity kte;
	@Inject(method = "tick", at = @At("HEAD")) private void tick(CallbackInfo callbackInfo) {
		if (!Config.enableKineticEffect.get()) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.isPaused() || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
		if (blockHitResult.getType() == HitResult.Type.MISS) return;
		if (!blockHitResult.getBlockPos().equals(kte.getBlockPos())) return;
		float speed = kte.getSpeed();
		if (speed == 0) return;
		BlockState state = kte.getBlockState();
		if (!(state.getBlock() instanceof KineticBlock kineticBlock)) return;
		Level level = kte.getLevel();
		if (level == null) return;
		Axis rotationAxis = kineticBlock.getRotationAxis(state);
		if (rotationAxis == null) return;
		Vec3 center = VecHelper.getCenterOf(kte.getBlockPos());
		SpeedLevel speedLevel = SpeedLevel.of(speed);
		level.addParticle(
				new RotationIndicatorParticleData(
						speedLevel.getColor(),
						speedLevel.getParticleSpeed() * Math.signum(speed),
						kineticBlock.getParticleInitialRadius(),
						kineticBlock.getParticleTargetRadius(),
						10,
						rotationAxis
				), center.x, center.y, center.z, 0, 0, 0
		);
	}
}
