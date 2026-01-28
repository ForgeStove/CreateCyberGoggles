package io.github.forgestove.create_cyber_goggles.mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.zurrtum.create.client.content.schematics.client.SchematicAndQuillHandler;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(SchematicAndQuillHandler.class)
public abstract class SchematicAndQuillHandlerMixin {
	@WrapMethod(method = "isActive")
	public boolean isActive(Minecraft mc, Operation<Boolean> original) {
		return CCGKey.useSchematic.keyMapping.isDown() && mc.screen == null || original.call(mc);
	}
}
