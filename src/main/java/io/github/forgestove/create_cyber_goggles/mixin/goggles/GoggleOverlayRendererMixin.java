package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.zurrtum.create.client.catnip.outliner.Outliner.OutlineEntry;
import com.zurrtum.create.client.content.equipment.goggles.GoggleOverlayRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.isInGame;
@Mixin(GoggleOverlayRenderer.class)
public abstract class GoggleOverlayRendererMixin {
	@Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
	private static void renderOverlay(CallbackInfo ci) {
		if (!CCG.CONFIG.goggles.disableScreenGoggles || isInGame()) return;
		ci.cancel();
	}
	@WrapOperation(
		method = "renderOverlay", at = @At(
		value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"
	)
	)
	private static Collection<OutlineEntry> wrapCollection(
		Map<Object, OutlineEntry> instance,
		Operation<Collection<OutlineEntry>> original
	) {
		return CCG.CONFIG.goggles.canRenderOnValueBox ? Collections.emptyList() : original.call(instance);
	}
}
