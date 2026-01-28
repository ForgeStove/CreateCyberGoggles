package io.github.forgestove.create_cyber_goggles.mixin.wrench;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.zurrtum.create.client.content.contraptions.chassis.ChassisRangeDisplay;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ChassisRangeDisplay.class)
public abstract class ChassisRangeDisplayMixin {
	@WrapOperation(
		method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
	)
	private static boolean tick(ItemStack instance, Item item, Operation<Boolean> original) {
		return CCG.CONFIG.wrench.alwaysShowScrollValue || original.call(instance, item);
	}
}
