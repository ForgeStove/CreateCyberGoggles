package io.github.forgestove.create_cyber_goggles.core.event;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import com.simibubi.create.infrastructure.config.AllConfigs;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.ItemRenderable;
import io.github.forgestove.create_cyber_goggles.core.factory.*;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientFluidEntryTooltipComponent.FluidEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientItemEntryTooltipComponent.ItemEntryTooltipComponent;
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
		var itemEntryComponent = new ItemEntryTooltipComponent(itemStack, 0, itemStack.getHoverName());
		components.set(0, ClientTooltipComponent.create(itemEntryComponent));
		var tooltipWidth = 0;
		var tooltipHeight = 0;
		for (var component : components) {
			tooltipWidth = Math.max(tooltipWidth, component.getWidth(mc.font));
			tooltipHeight += component.getHeight();
		}
		// 物品悬浮框淡入淡出全程停留在该偏移位置
		if (liftAboveGoggle) y -= tooltipHeight + 10;
		// 触底：期望位置（未 clamp）底部越界 → 整体缩放
		if (y + tooltipHeight > height)
			OverlayLayoutManager.overallScale = Math.min(OverlayLayoutManager.overallScale, (float) height / (y + tooltipHeight));
		x = Mth.clamp(x, 0, width - tooltipWidth);
		y = Mth.clamp(y, 16, height - tooltipHeight - 100);
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
		var fluidEntries = new ArrayList<FluidEntryTooltipComponent>();
		for (var i = 0; i < tooltipLines.size(); i++) {
			var line = tooltipLines.get(i);
			var item = TooltipComponentUtil.removeItemEntry(line);
			if (item != null) {
				parsed.add(new ClientItemEntryTooltipComponent(item.stack(), item.indent(), item.label()));
				continue;
			}
			var fluid = TooltipComponentUtil.removeFluidEntry(line);
			if (fluid != null) {
				parsed.add(fluid);
				fluidEntries.add(fluid);
				continue;
			}
			var itemList = TooltipComponentUtil.removeItemList(line);
			if (itemList != null) {
				parsed.add(new ClientItemListTooltipComponent(itemList.items(), itemList.indent(), itemList.maxColumns()));
				continue;
			}
			var fluidList = TooltipComponentUtil.removeFluidList(line);
			if (fluidList != null) {
				parsed.add(new ClientFluidListTooltipComponent(fluidList.fluids(), fluidList.indent(), fluidList.maxColumns()));
				continue;
			}
			if (firstLinePadding && i == 0) line = Component.literal(" ".repeat(Mth.ceil(16F / mc.font.width(" ")))).append(line);
			parsed.addAll(mc.font.split(line, effectiveMaxWidth));
		}
		var sharedFluidWidth = 0;
		for (var fluidEntry : fluidEntries) {
			var preferred = ClientFluidEntryTooltipComponent.preferredBarWidth(mc.font, fluidEntry.fluid(), fluidEntry.capacityMb());
			if (preferred > sharedFluidWidth) sharedFluidWidth = preferred;
		}
		var components = new ArrayList<ClientTooltipComponent>(parsed.size());
		for (var value : parsed) {
			if (value instanceof ClientTooltipComponent client) {
				components.add(client);
				continue;
			}
			if (value instanceof FluidEntryTooltipComponent(var fluid, var indent, var capacityMb)) {
				components.add(new ClientFluidEntryTooltipComponent(fluid, indent, capacityMb, sharedFluidWidth));
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
		if (ClientHooks.onRenderTooltipPre(itemStack, gui, x, y, width, height, components, mc.font, positioner).isCanceled()) return;
		var tooltipPos = positioner.positionTooltip(width, height, x, y, tooltipWidth, tooltipHeight);
		var tooltipX = tooltipPos.x();
		var tooltipY = tooltipPos.y() + OverlayLayoutManager.overallOffsetY;
		// 触底：期望位置底部越界 → 更新整体缩放（供所有 overlay 下一帧使用）
		if (y + tooltipHeight > height)
			OverlayLayoutManager.overallScale = Math.min(OverlayLayoutManager.overallScale, (float) height / (y + tooltipHeight));
		var pose = gui.pose();
		pose.pushPose();
		// 整体缩放（最靠底触底时，三个 overlay 一起围绕整体锚点缩放）
		if (OverlayLayoutManager.overallScale < 1) {
			pose.translate(OverlayLayoutManager.scaleCenterX, OverlayLayoutManager.scaleCenterY, 0);
			pose.scale(OverlayLayoutManager.overallScale, OverlayLayoutManager.overallScale, 1);
			pose.translate(-OverlayLayoutManager.scaleCenterX, -OverlayLayoutManager.scaleCenterY, 0);
		}
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
		// 登记已渲染区域，供后渲染的 TooltipRenderer 避让
		OverlayLayoutManager.occupy(tooltipX, tooltipY, tooltipWidth, tooltipHeight);
		pose.popPose();
	}
}
