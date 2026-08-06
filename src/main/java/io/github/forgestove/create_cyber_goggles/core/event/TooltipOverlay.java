package io.github.forgestove.create_cyber_goggles.core.event;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import com.simibubi.create.infrastructure.config.AllConfigs;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.ItemRenderable;
import io.github.forgestove.create_cyber_goggles.core.factory.*;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientFluidEntryTooltipComponent.FluidEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientFluidListTooltipComponent.FluidListTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientItemEntryTooltipComponent.ItemEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientItemListTooltipComponent.ItemListTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.TooltipTheme.Theme;
import io.github.forgestove.create_cyber_goggles.core.util.TooltipComponentUtil;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.*;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public final class TooltipOverlay {
	public static float hoverTicks;
	private static ItemStack lastItemStack = ItemStack.EMPTY;
	private static boolean liftAboveGoggle;
	public static void register(@NotNull RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.HOTBAR, getCCGRes("tooltip_overlay"), TooltipOverlay::renderOverlay);
	}
	public static void renderOverlay(GuiGraphics gui, DeltaTracker deltaTracker) {
		if (!CCG.config.overlay.renderItemOverlay || !CCG.config.goggles.gameMode.enableGoggles) return;
		if (shouldSuppressInfo()) return;
		if (mc.isPaused() || isInGUI() || mc.options.hideGui) {
			hoverTicks = 0;
			return;
		}
		if (!CCG.config.goggles.canRenderOnValueBox && hasActivedValueBox()) return;
		var itemStack = toRenderItemStack();
		if (itemStack.isEmpty()) {
			if (CCG.config.goggles.enableFadeOut && hoverTicks > 0) {
				hoverTicks = Math.max(0, hoverTicks - getRealtimeDeltaTicks() * 2);
				renderItemStack(gui, lastItemStack);
			} else hoverTicks = 0;
			return;
		}
		hoverTicks = Math.min(8, hoverTicks + getRealtimeDeltaTicks());
		lastItemStack = itemStack;
		// 锁存"是否抬到目镜信息上方"，使淡出期间保持在偏移后的位置，而非随护目镜层的 hoverTicks 提前落回原位
		liftAboveGoggle = GoggleOverlayRenderer.hoverTicks > 0;
		renderItemStack(gui, itemStack);
	}
	public static @NotNull ItemStack toRenderItemStack() {
		try {
			if (getBlockEntity() instanceof ItemRenderable ir) return orEmpty(ir.ccg$getItemStack());
			if (getEntity() instanceof ItemRenderable ir) return orEmpty(ir.ccg$getItemStack());
		} catch (Throwable e) {
			CCG.LOGGER.error("Failed to get item stack", e);
		}
		return ItemStack.EMPTY;
	}
	public static void renderItemStack(@NotNull GuiGraphics gui, @NotNull ItemStack itemStack) {
		if (itemStack.isEmpty()) return;
		var pose = gui.pose();
		pose.pushPose();
		var overlay = CCG.config.overlay;
		var cfg = AllConfigs.client();
		var theme = getTheme();
		var back = theme.backColor();
		var top = theme.topColor();
		var bot = theme.botColor();
		var fade = Mth.clamp(hoverTicks / 8F, 0, 1);
		if (fade < 1) {
			pose.translate(Math.pow(1 - fade, 3) * Math.signum(cfg.overlayOffsetX.get() + 0.5D) * 8, 0, 0);
			back.scaleAlpha(fade);
			top.scaleAlpha(fade);
			bot.scaleAlpha(fade);
		}
		var width = gui.guiWidth();
		var height = gui.guiHeight();
		var x = width / 2 + cfg.overlayOffsetX.get() + overlay.overlayPos.x;
		var y = height / 2 + cfg.overlayOffsetY.get() + overlay.overlayPos.y;
		if (overlay.tooltipFlagType == null) overlay.tooltipFlagType = TooltipFlagType.Default;
		var tooltipLines = itemStack.getTooltipLines(TooltipContext.of(mc.level), mc.player, overlay.tooltipFlagType.getFlag());
		var components = buildTooltipComponents(tooltipLines, width - x - 16, true);
		// tooltipLines 可能为空（部分物品无 tooltip 行），此时 components 为空，需用 add 而非 set
		var itemEntryComponent = ClientTooltipComponent.create(new ItemEntryTooltipComponent(itemStack, 0, itemStack.getHoverName()));
		if (components.isEmpty()) components.add(itemEntryComponent);
		else components.set(0, itemEntryComponent);
		var tooltipWidth = 0;
		var tooltipHeight = 0;
		for (var component : components) {
			tooltipWidth = Math.max(tooltipWidth, component.getWidth(mc.font));
			tooltipHeight += component.getHeight();
		}
		// 物品悬浮框淡入淡出全程停留在该偏移位置
		if (liftAboveGoggle) y -= tooltipHeight + 10;
		// 顶部下限固定 16：悬浮框过高导致 height - tooltipHeight - 100 < 16 时不得超顶
		x = Mth.clamp(x, 0, width - tooltipWidth);
		y = Mth.clamp(y, 16, Math.max(16, height - tooltipHeight - 100));
		renderTooltip(gui, itemStack, components, x, y, tooltipWidth, tooltipHeight, back.getRGB(), top.getRGB(), bot.getRGB());
		pose.popPose();
	}
	public static @NotNull Theme getTheme() {
		var overlay = CCG.config.overlay;
		var useCCGCustom = overlay.useCustomColor;
		if (!useCCGCustom) {
			if (overlay.tooltipTheme == null) overlay.tooltipTheme = TooltipTheme.Default;
			var theme = overlay.tooltipTheme.theme;
			if (theme != null) return theme;
			var cfg = AllConfigs.client();
			var useCreateCustom = cfg.overlayCustomColor.get();
			var back = useCreateCustom
				? new Color(cfg.overlayBackgroundColor.get())
				: BoxElement.COLOR_VANILLA_BACKGROUND.scaleAlpha(0.75F);
			var top = useCreateCustom ? new Color(cfg.overlayBorderColorTop.get()) : BoxElement.COLOR_VANILLA_BORDER.getFirst().copy();
			var bot = useCreateCustom ? new Color(cfg.overlayBorderColorBot.get()) : BoxElement.COLOR_VANILLA_BORDER.getSecond().copy();
			return new Theme(back, top, bot);
		}
		return new Theme(overlay.backgroundColor, overlay.borderTopColor, overlay.borderBottomColor);
	}
	public static @NotNull List<ClientTooltipComponent> buildTooltipComponents(
		@NotNull List<Component> tooltipLines,
		int maxWidth,
		boolean firstLinePadding
	) {
		var effectiveMaxWidth = maxWidth > 0 ? maxWidth : Integer.MAX_VALUE;
		var parsed = new ArrayList<>();
		for (var i = 0; i < tooltipLines.size(); i++) {
			var line = tooltipLines.get(i);
			var split = TooltipComponentUtil.consumeMarker(line);
			if (split != null) {
				// marker 与普通文本混行：剩余文本单独成行，UI 独立成行
				var remaining = split.remaining();
				if (remaining != null) parsed.addAll(mc.font.split(remaining, effectiveMaxWidth));
				parsed.add(split.data());
				continue;
			}
			if (firstLinePadding && i == 0) line = Component.literal(" ".repeat(Mth.ceil(16F / mc.font.width(" ")))).append(line);
			parsed.addAll(mc.font.split(line, effectiveMaxWidth));
		}
		var sharedFluidWidth = 0;
		for (var value : parsed)
			if (value instanceof FluidEntryTooltipComponent fluidEntry) {
				var preferred = ClientFluidEntryTooltipComponent.preferredBarWidth(mc.font, fluidEntry.fluid(), fluidEntry.capacityMb());
				if (preferred > sharedFluidWidth) sharedFluidWidth = preferred;
			}
		var components = new ArrayList<ClientTooltipComponent>(parsed.size());
		for (var value : parsed) {
			if (value instanceof ClientTooltipComponent client) {
				components.add(client);
				continue;
			}
			if (value instanceof FluidEntryTooltipComponent fluidEntry) {
				components.add(new ClientFluidEntryTooltipComponent(
					fluidEntry.fluid(),
					fluidEntry.indent(),
					fluidEntry.capacityMb(),
					sharedFluidWidth
				));
				continue;
			}
			if (value instanceof ItemEntryTooltipComponent(var stack, var indent, var label)) {
				components.add(new ClientItemEntryTooltipComponent(stack, indent, label));
				continue;
			}
			if (value instanceof ItemListTooltipComponent(var items, var indent, var maxColumns)) {
				components.add(new ClientItemListTooltipComponent(items, indent, maxColumns));
				continue;
			}
			if (value instanceof FluidListTooltipComponent(var fluids, var indent, var maxColumns)) {
				components.add(new ClientFluidListTooltipComponent(fluids, indent, maxColumns));
				continue;
			}
			if (value instanceof FormattedCharSequence text) components.add(ClientTooltipComponent.create(text));
		}
		return components;
	}
	public static void renderTooltip(
		GuiGraphics gui,
		ItemStack itemStack,
		@NotNull List<ClientTooltipComponent> components,
		int x,
		int y,
		int tooltipWidth,
		int tooltipHeight,
		int back,
		int top,
		int bot
	) {
		if (components.isEmpty()) return;
		var width = gui.guiWidth();
		var height = gui.guiHeight();
		var positioner = DefaultTooltipPositioner.INSTANCE;
		// 触发 Pre 事件：面板（renderTooltipPre）与悬浮框共用同一锚点；用 preEvent 位置定位以响应 setY 让位
		var preEvent = ClientHooks.onRenderTooltipPre(itemStack, gui, x, y, width, height, components, mc.font, positioner);
		if (preEvent.isCanceled()) return;
		var tooltipPos = positioner.positionTooltip(width, height, preEvent.getX(), preEvent.getY(), tooltipWidth, tooltipHeight);
		var tooltipX = tooltipPos.x();
		var tooltipY = tooltipPos.y();
		var pose = gui.pose();
		pose.pushPose();
		TooltipRenderUtil.renderTooltipBackground(gui, tooltipX, tooltipY, tooltipWidth, tooltipHeight, 400, back, back, top, bot);
		pose.translate(0, 0, 400);
		// 用独立BufferSource渲染文字并立即提交(防止后续renderImage干扰文字shader state)，再单独循环渲染所有图片
		try (var bbBuilder = new ByteBufferBuilder(256)) {
			var textBuffer = MultiBufferSource.immediate(bbBuilder);
			var textY = tooltipY;
			for (var i = 0; i < components.size(); i++) {
				var component = components.get(i);
				component.renderText(mc.font, tooltipX, textY, pose.last().pose(), textBuffer);
				textY += component.getHeight() + (i == 0 ? 2 : 0);
			}
			textBuffer.endBatch();
		}
		var textY = tooltipY;
		for (var i = 0; i < components.size(); i++) {
			var component = components.get(i);
			component.renderImage(mc.font, tooltipX, textY, gui);
			textY += component.getHeight() + (i == 0 ? 2 : 0);
		}
		// 登记已渲染区域供 Goggle 避让；Goggle 自身 tooltip（EMPTY）不登记，避免其自我避让导致持续下移
		if (!itemStack.isEmpty()) OverlayManager.upperBottom = Math.max(OverlayManager.upperBottom, tooltipY + tooltipHeight);
		pose.popPose();
	}
}
