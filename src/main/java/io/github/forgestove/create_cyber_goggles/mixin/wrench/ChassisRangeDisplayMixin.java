package io.github.forgestove.create_cyber_goggles.mixin.wrench;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.content.contraptions.chassis.ChassisRangeDisplay;
import com.tterrag.registrate.util.entry.ItemEntry;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ChassisRangeDisplay.class)
public abstract class ChassisRangeDisplayMixin {
	@WrapOperation(
		method = "tick",
		at = @At(value = "INVOKE", target = "Lcom/tterrag/registrate/util/entry/ItemEntry;isIn(Lnet/minecraft/world/item/ItemStack;)Z")
	)
	private static boolean tick(ItemEntry<?> instance, ItemStack stack, Operation<Boolean> original) {
		return CCG.CONFIG.wrench.alwaysShowScrollValue || original.call(instance, stack);
	}
}
