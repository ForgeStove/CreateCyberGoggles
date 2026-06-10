package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(BasinBlockEntity.class)
public interface BasinBlockEntityAccessor {
	@Accessor
	SmartFluidTankBehaviour getOutputTank();
}
