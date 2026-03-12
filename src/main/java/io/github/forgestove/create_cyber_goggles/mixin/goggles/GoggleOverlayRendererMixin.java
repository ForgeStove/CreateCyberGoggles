package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.TooltipOverlay;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.createmod.catnip.outliner.Outliner.OutlineEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.isInGame;
@Mixin(GoggleOverlayRenderer.class)
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
	)
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
		var hasItemList = false;
		for (var line : tooltip) {
			if (!CCGLang.hasItemList(line) && !CCGLang.hasItemEntry(line)) continue;
			hasItemList = true;
			break;
		}
		if (!hasItemList) {
			original.call(graphics, tooltip, x, y, screenWidth, screenHeight, maxWidth, back, top, bot, font);
			return;
		}
		var components = TooltipOverlay.buildTooltipComponents(tooltip, maxWidth, false);
		if (components.isEmpty()) return;
		var tooltipWidth = components.stream().mapToInt(c -> c.getWidth(Minecraft.getInstance().font)).max().orElse(0);
		var tooltipHeight = components.stream().mapToInt(ClientTooltipComponent::getHeight).sum() + (components.size() > 1 ? 2 : 0);
		TooltipOverlay.renderTooltip(graphics, ItemStack.EMPTY, components, x, y, tooltipWidth, tooltipHeight, back, top, bot);
	}
}
