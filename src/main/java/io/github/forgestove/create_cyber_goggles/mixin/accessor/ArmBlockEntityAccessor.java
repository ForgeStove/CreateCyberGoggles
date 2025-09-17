package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.simibubi.create.content.kinetics.mechanicalArm.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
@Mixin(ArmBlockEntity.class)
public interface ArmBlockEntityAccessor {
	@Accessor
	List<ArmInteractionPoint> getInputs();
	@Accessor
	List<ArmInteractionPoint> getOutputs();
}
