package io.github.forgestove.create_cyber_goggles.mixin.compact.simulated;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.AllTags.AllItemTags;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ZiplineClientManager;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ZiplineClientManager.class)
public abstract class ZiplineClientManagerMixin {
	@SuppressWarnings("MixinAnnotationTarget")
	@WrapOperation(
		method = "*",
		at = @At(value = "INVOKE", target = "Lcom/simibubi/create/AllTags$AllItemTags;matches(Lnet/minecraft/world/item/ItemStack;)Z")
	)
	private static boolean ridingTick(AllItemTags instance, ItemStack stack, Operation<Boolean> original) {
		if (!CCG.config.aeronautics.alwaysAllowRidingRope) return original.call(instance, stack);
		return true;
	}
}
