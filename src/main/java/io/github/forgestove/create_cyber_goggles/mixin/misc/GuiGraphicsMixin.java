package io.github.forgestove.create_cyber_goggles.mixin.misc;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import io.github.forgestove.create_cyber_goggles.core.event.TooltipOverlay;
import io.github.forgestove.create_cyber_goggles.core.util.TooltipComponentUtil;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin implements Self<GuiGraphics> {
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@Inject(
		method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void renderTooltip(
		Font font,
		List<Component> tooltipLines,
		Optional<TooltipComponent> visualTooltipComponent,
		int mouseX,
		int mouseY,
		CallbackInfo ci
	) {
		if (tooltipLines == null || tooltipLines.isEmpty()) return;
		var hasIconMarkers = false;
		for (var line : tooltipLines) {
			if (!TooltipComponentUtil.hasIcon(line)) continue;
			hasIconMarkers = true;
			break;
		}
		if (!hasIconMarkers) return;
		var components = TooltipOverlay.buildTooltipComponents(tooltipLines, Integer.MAX_VALUE, false);
		visualTooltipComponent.ifPresent(tooltipComponent -> components.add(ClientTooltipComponent.create(tooltipComponent)));
		if (components.isEmpty()) return;
		var tooltipWidth = components.stream().mapToInt(component -> component.getWidth(font)).max().orElse(0);
		var tooltipHeight = TooltipOverlay.calculateTooltipHeight(components);
		TooltipOverlay.renderTooltip(
			thiz(),
			components,
			mouseX,
			mouseY,
			tooltipWidth,
			tooltipHeight,
			TooltipOverlay.VANILLA_BACKGROUND,
			TooltipOverlay.VANILLA_BORDER_TOP,
			TooltipOverlay.VANILLA_BORDER_BOTTOM,
			DefaultTooltipPositioner.INSTANCE
		);
		ci.cancel();
	}
}
