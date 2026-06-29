package io.github.forgestove.create_cyber_goggles.mixin.wrench;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.foundation.blockEntity.behaviour.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.isServer;
@Mixin(ValueSettingsInputHandler.class)
public abstract class ValueSettingsInputHandlerMixin {
	@WrapOperation(
		method = "onBlockActivated",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z")
	)
	private static boolean tick(ItemStack instance, TagKey<Item> tag, Operation<Boolean> original) {
		return isServer() ? original.call(instance, tag) : CCG.config.misc.wrench.alwaysShowScrollValue || original.call(instance, tag);
	}
	@WrapOperation(
		method = "onBlockActivated", at = @At(
		value = "INVOKE", target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/ValueSettingsBehaviour;onlyVisibleWithWrench()Z"
	)
	)
	private static boolean tick(ValueSettingsBehaviour instance, Operation<Boolean> original) {
		return isServer() ? original.call(instance) : CCG.config.misc.wrench.alwaysShowScrollValue || original.call(instance);
	}
}
