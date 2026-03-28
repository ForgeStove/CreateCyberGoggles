package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.TooltipOverlay;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.createmod.catnip.outliner.Outliner.OutlineEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.isInGame;
@Mixin(value = GoggleOverlayRenderer.class, remap = false)
public abstract class GoggleOverlayRendererMixin {
	@Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
	private static void renderOverlay(CallbackInfo ci) {
		if (!CCG.config.goggles.disableScreenGoggles || isInGame()) return;
		ci.cancel();
	}
	@WrapOperation(
		method = "renderOverlay", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPlayerMode()Lnet/minecraft/world/level/GameType;"
	), remap = true
	)
	private static GameType wrapGameMode(MultiPlayerGameMode instance, Operation<GameType> original) {
		return CCG.config.gameMode.enableInSpectator ? null : original.call(instance);
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
	@WrapOperation(
		method = "renderOverlay", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/foundation/gui/RemovedGuiUtils;drawHoveringText(Lnet/minecraft/client/gui/GuiGraphics;"
			+ "Ljava/util/List;IIIIIIIILnet/minecraft/client/gui/Font;)V"
	)
	)
	private static void wrapTooltipRender(
		GuiGraphics graphics,
		List<Component> tooltip,
		int mouseX,
		int mouseY,
		int screenWidth,
		int screenHeight,
		int maxTextWidth,
		int backgroundColor,
		int borderColorStart,
		int borderColorEnd,
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
			original.call(
				graphics,
				tooltip,
				mouseX,
				mouseY,
				screenWidth,
				screenHeight,
				maxTextWidth,
				backgroundColor,
				borderColorStart,
				borderColorEnd,
				font
			);
			return;
		}
		var components = TooltipOverlay.buildTooltipComponents(tooltip, maxTextWidth, false);
		if (components.isEmpty()) return;
		var tooltipWidth = components.stream().mapToInt(c -> c.getWidth(Minecraft.getInstance().font)).max().orElse(0);
		var tooltipHeight = TooltipOverlay.calculateTooltipHeight(components);
		TooltipOverlay.renderTooltip(
			graphics,
			components,
			mouseX,
			mouseY,
			tooltipWidth,
			tooltipHeight,
			backgroundColor,
			borderColorStart,
			borderColorEnd,
			DefaultTooltipPositioner.INSTANCE
		);
	}
}
