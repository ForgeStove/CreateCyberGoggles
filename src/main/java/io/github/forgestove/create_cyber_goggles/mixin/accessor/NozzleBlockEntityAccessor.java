package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.zurrtum.create.content.kinetics.fan.NozzleBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value = NozzleBlockEntity.class, remap = false)
public interface NozzleBlockEntityAccessor {
	@Accessor
	boolean getPushing();
	@Accessor
	float getRange();
}
