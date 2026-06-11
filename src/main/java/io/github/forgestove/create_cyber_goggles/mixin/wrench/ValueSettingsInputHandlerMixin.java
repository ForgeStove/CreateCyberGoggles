package io.github.forgestove.create_cyber_goggles.mixin.wrench;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.isServer;
@Mixin(ValueSettingsInputHandler.class)
public abstract class ValueSettingsInputHandlerMixin {
	@WrapOperation(
		method = "handleInteraction",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z")
	)
	private static boolean tick(ItemStack instance, TagKey<?> tagKey, Operation<Boolean> original) {
		return isServer() ? original.call(instance, tagKey) : CCG.config.wrench.alwaysShowScrollValue || original.call(instance, tagKey);
	}
	@WrapOperation(
		method = "handleInteraction", at = @At(
		value = "INVOKE",
		target = "Lcom/zurrtum/create/client/foundation/blockEntity/behaviour/ValueSettingsBehaviour;onlyVisibleWithWrench()Z"
	)
	)
	private static boolean tick(ValueSettingsBehaviour instance, Operation<Boolean> original) {
		return isServer() ? original.call(instance) : CCG.config.wrench.alwaysShowScrollValue || original.call(instance);
	}
}
