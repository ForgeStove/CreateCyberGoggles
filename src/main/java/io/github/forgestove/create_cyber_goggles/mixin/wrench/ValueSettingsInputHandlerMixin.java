package io.github.forgestove.create_cyber_goggles.mixin.wrench;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.isServer;
@Mixin(ValueSettingsInputHandler.class)
public abstract class ValueSettingsInputHandlerMixin {
	@WrapOperation(
		method = "handleInteraction", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/tags/TagKey;)Z")
	)
	private static boolean tick(Holder<?> instance, TagKey<?> tTagKey, Operation<Boolean> original) {
		return isServer() ? original.call(instance, tTagKey) : CCG.CONFIG.wrench.alwaysShowScrollValue || original.call(instance, tTagKey);
	}
	@WrapOperation(
		method = "handleInteraction", at = @At(
		value = "INVOKE",
		target = "Lcom/zurrtum/create/client/foundation/blockEntity/behaviour/ValueSettingsBehaviour;onlyVisibleWithWrench()Z"
	)
	)
	private static boolean tick(ValueSettingsBehaviour instance, Operation<Boolean> original) {
		return isServer() ? original.call(instance) : CCG.CONFIG.wrench.alwaysShowScrollValue || original.call(instance);
	}
}
