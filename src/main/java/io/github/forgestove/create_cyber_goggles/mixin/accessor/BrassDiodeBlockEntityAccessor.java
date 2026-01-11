package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.zurrtum.create.content.redstone.diodes.BrassDiodeBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(BrassDiodeBlockEntity.class)
public interface BrassDiodeBlockEntityAccessor {
	@Accessor
	ServerScrollValueBehaviour getMaxState();
}
