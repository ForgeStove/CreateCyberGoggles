package io.github.forgestove.create_cyber_goggles.mixin.misc.wrench;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueRenderer;
import com.tterrag.registrate.util.entry.ItemEntry;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ScrollValueRenderer.class)
public abstract class ScrollValueRendererMixin {
	@WrapOperation(
		method = "tick",
		at = @At(value = "INVOKE", target = "Lcom/tterrag/registrate/util/entry/ItemEntry;isIn(Lnet/minecraft/world/item/ItemStack;)Z")
	)
	private static boolean tick(ItemEntry<?> instance, ItemStack stack, Operation<Boolean> original) {
		return CCG.config.misc.wrench.alwaysShowScrollValue || original.call(instance, stack);
	}
}
