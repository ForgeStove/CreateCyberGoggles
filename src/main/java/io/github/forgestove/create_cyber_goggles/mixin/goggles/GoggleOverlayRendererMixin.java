package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GoggleOverlayRenderer.class)
public abstract class GoggleOverlayRendererMixin {
	@Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
	private static void renderOverlay(CallbackInfo callbackInfo) {
		if (!CCG.CONFIG.goggles.disableScreenGoggles || Minecraft.getInstance().screen == null) return;
		callbackInfo.cancel();
	}
}
