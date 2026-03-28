package io.github.forgestove.create_cyber_goggles.mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.content.schematics.client.SchematicAndQuillHandler;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import org.spongepowered.asm.mixin.Mixin;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Mixin(value = SchematicAndQuillHandler.class, remap = false)
public abstract class SchematicAndQuillHandlerMixin {
	@WrapMethod(method = "isActive")
	public boolean isActive(Operation<Boolean> original) {
		return CCGKey.useSchematic.isDown() && mc.screen == null || original.call();
	}
}
