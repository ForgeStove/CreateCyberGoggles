package io.github.forgestove.create_cyber_goggles.mixin.wrench;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.foundation.blockEntity.behaviour.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.isServer;
@Mixin(value = ValueSettingsInputHandler.class, remap = false)
public abstract class ValueSettingsInputHandlerMixin {
	@WrapOperation(
		method = "onBlockActivated",
		at = @At(value = "INVOKE", target = "Lcom/simibubi/create/AllTags$AllItemTags;matches(Lnet/minecraft/world/item/ItemStack;)Z")
	)
	private static boolean tick(AllItemTags instance, ItemStack stack, Operation<Boolean> original) {
		return isServer() ? original.call(instance, stack) : CCG.config.wrench.alwaysShowScrollValue || original.call(instance, stack);
	}
	@WrapOperation(
		method = "onBlockActivated", at = @At(
		value = "INVOKE", target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/ValueSettingsBehaviour;onlyVisibleWithWrench()Z"
	)
	)
	private static boolean tick(ValueSettingsBehaviour instance, Operation<Boolean> original) {
		return isServer() ? original.call(instance) : CCG.config.wrench.alwaysShowScrollValue || original.call(instance);
	}
}
