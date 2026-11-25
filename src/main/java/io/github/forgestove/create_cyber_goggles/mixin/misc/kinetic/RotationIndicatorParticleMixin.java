package io.github.forgestove.create_cyber_goggles.mixin.misc.kinetic;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.content.kinetics.base.RotationIndicatorParticle;
import dev.ryanhcode.sable.Sable;
import io.github.forgestove.create_cyber_goggles.foundation.util.CCGMods;
import io.github.forgestove.create_cyber_goggles.mixin.ModCondition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static io.github.forgestove.create_cyber_goggles.foundation.util.CCGUtil.mc;
@ModCondition(require = CCGMods.sable)
@Mixin(RotationIndicatorParticle.class)
public abstract class RotationIndicatorParticleMixin {
	@WrapOperation(
		method = "move", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"
	)
	)
	private Vec3 projectOutOfSubLevel(Vec3 rotatedOffset, Vec3 origin, Operation<Vec3> original) {
		Vec3 position = original.call(rotatedOffset, origin);
		if (mc.level == null) return position;
		return Sable.HELPER.projectOutOfSubLevel(mc.level, position);
	}
}
