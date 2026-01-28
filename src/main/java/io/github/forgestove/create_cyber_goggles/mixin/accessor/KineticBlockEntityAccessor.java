package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(KineticBlockEntity.class)
public interface KineticBlockEntityAccessor {
	@Accessor
	float getStress();
	@Accessor
	float getCapacity();
}
