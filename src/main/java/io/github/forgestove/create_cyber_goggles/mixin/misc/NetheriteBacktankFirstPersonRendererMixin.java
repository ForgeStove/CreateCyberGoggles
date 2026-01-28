package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.zurrtum.create.client.content.equipment.armor.NetheriteBacktankFirstPersonRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(NetheriteBacktankFirstPersonRenderer.class)
public abstract class NetheriteBacktankFirstPersonRendererMixin {
	@Inject(method = "getHandTexture", at = @At("HEAD"), cancellable = true)
	private static void getHandTexture(LocalPlayer player, CallbackInfoReturnable<ResourceLocation> cir) {
		if (!CCG.CONFIG.misc.removeNetheriteFirstPerson) return;
		cir.setReturnValue(null);
	}
}
