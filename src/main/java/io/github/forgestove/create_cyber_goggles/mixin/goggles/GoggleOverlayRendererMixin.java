package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.zurrtum.create.client.catnip.outliner.Outliner.OutlineEntry;
import com.zurrtum.create.client.content.equipment.goggles.GoggleOverlayRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.TooltipOverlay;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientItemEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.client.gui.*;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(GoggleOverlayRenderer.class)
public abstract class GoggleOverlayRendererMixin {
	@Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
	private static void renderOverlay(CallbackInfo ci) {
		if (!CCG.config.goggles.disableScreenGoggles || isInGame()) return;
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
		return CCG.config.goggles.canRenderOnValueBox ? Collections.emptyList() : original.call(instance);
	}
	@WrapOperation(method = "renderOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;removeLast()Ljava/lang/Object;"))
	private static Object wrapRemove(List<Component> instance, Operation<?> original) {
		if (instance.isEmpty()) return null;
		return original.call(instance);
	}
	@WrapOperation(
		method = "renderOverlay", at = @At(
		value = "INVOKE",
		target =
			"Lcom/zurrtum/create/client/foundation/gui/RemovedGuiUtils;drawHoveringText(Lnet/minecraft/client/gui/GuiGraphicsExtractor;"
				+ "Ljava/util/List;IIIIIIIILnet/minecraft/client/gui/Font;)V"
	)
	)
	private static void wrapTooltipRender(
		GuiGraphicsExtractor gui,
		List<Component> tooltip,
		int x,
		int y,
		int screenWidth,
		int screenHeight,
		int maxWidth,
		int back,
		int top,
		int bot,
		Font font,
		Operation<Void> original
	) {
		if (CCG.config.goggles.dedupTooltipLines) tooltip = GoggleTooltipDedupUtil.dedupAdjacentLines(tooltip);
		var hasItemList = false;
		for (var line : tooltip) {
			if (!TooltipComponentUtil.hasIcon(line)) continue;
			hasItemList = true;
			break;
		}
		if (!hasItemList) {
			original.call(gui, tooltip, x, y, screenWidth, screenHeight, maxWidth, back, top, bot, font);
			return;
		}
		var components = TooltipOverlay.buildTooltipComponents(tooltip, maxWidth, false);
		if (components.isEmpty()) return;
		var tooltipWidth = components.stream().mapToInt(c -> c.getWidth(mc.font)).max().orElse(0);
		var tooltipHeight = components.stream().mapToInt(component -> component.getHeight(mc.font)).sum() + (
			components.size() > 1 ? 2 : 0
		);
		var tooltipPos = TooltipOverlay.renderTooltip(gui, components, x, y, tooltipWidth, tooltipHeight, back, top, bot);
		if (tooltipPos == null) return;
		for (var component : components) {
			if (!(component instanceof ClientItemEntryTooltipComponent entry)) continue;
			TooltipOverlay.renderTooltipOverlay(entry.stack(), gui, components, tooltipPos);
			break;
		}
	}
}
