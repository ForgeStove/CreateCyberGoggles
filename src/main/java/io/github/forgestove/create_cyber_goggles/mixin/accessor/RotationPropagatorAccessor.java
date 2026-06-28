package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(RotationPropagator.class)
public interface RotationPropagatorAccessor {
	@Invoker("getAxisModifier")
	static float getAxisModifier(KineticBlockEntity be, Direction direction) {
		throw new AssertionError();
	}
}
