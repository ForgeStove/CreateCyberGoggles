package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.simibubi.create.content.logistics.depot.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value = DepotBehaviour.class, remap = false)
public interface DepotBehaviourAccessor {
	@Accessor
	DepotItemHandler getItemHandler();
}
