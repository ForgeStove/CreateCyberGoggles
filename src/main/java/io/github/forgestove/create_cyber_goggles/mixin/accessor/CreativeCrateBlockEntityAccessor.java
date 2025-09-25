package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.simibubi.create.content.logistics.crate.CreativeCrateBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(CreativeCrateBlockEntity.class)
public interface CreativeCrateBlockEntityAccessor {
	@Accessor
	FilteringBehaviour getFiltering();
}
