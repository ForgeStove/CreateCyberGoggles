package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBox;
import com.simibubi.create.infrastructure.config.AllConfigs;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.outliner.Outliner.OutlineEntry;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.*;

import java.util.Map;
public class OverlayRenderer {
	public static final Map<Object, OutlineEntry> outlines = Outliner.getInstance().getOutlines();
	public static int hoverTicks;
	public static float fade;
	public static ItemStack currentItemStack;
	public static void registerLayer(@NotNull RegisterGuiLayersEvent event) {
		event.registerAbove(
			VanillaGuiLayers.HOTBAR,
			ResourceLocation.fromNamespaceAndPath(CCG.ID, "goggle_overlay"),
			OverlayRenderer::renderOverlay
		);
	}
	public static void tickColor(@NotNull RenderTooltipEvent.Color event) {
		if (!event.getItemStack().equals(currentItemStack) || currentItemStack.isEmpty()) return;
		var cfg = AllConfigs.client();
		var colorBackground = cfg.overlayCustomColor.get()
			? new Color(cfg.overlayBackgroundColor.get())
			: BoxElement.COLOR_VANILLA_BACKGROUND.scaleAlpha(.75f);
		if (fade < 1) colorBackground.scaleAlpha(fade);
		event.setBackground(colorBackground.getRGB());
	}
	public static void renderOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if (!CCG.CONFIG.goggles.renderExtraItems || !CCG.CONFIG.gameMode.enableGoggle) return;
		var mc = Minecraft.getInstance();
		if (mc.isPaused() || mc.screen != null || mc.options.hideGui) {
			currentItemStack = null;
			hoverTicks = 0;
			return;
		}
		if (!CCG.CONFIG.goggles.canRenderOnValueBox) for (var entry : outlines.values()) {
			if (!entry.isAlive()) continue;
			var outline = entry.getOutline();
			if (outline instanceof ValueBox && !((ValueBox) outline).isPassive) return;
		}
		fade = Mth.clamp((hoverTicks++ + deltaTracker.getGameTimeDeltaPartialTick(false)) / 24f, 0, 1);
		var itemStack = toRenderItemStack();
		currentItemStack = itemStack;
		if (itemStack == null || itemStack.isEmpty()) {
			hoverTicks = 0;
			return;
		}
		renderItemStack(guiGraphics, itemStack);
	}
	/**
	 * 根据当前玩家选中的方块实体或实体，返回待渲染的{@link ItemStack}。
	 *
	 * @return 需要渲染的 {@link ItemStack}，若无则为 {@code null}
	 */
	public static @Nullable ItemStack toRenderItemStack() {
		if (CCGUtil.getBE() instanceof IItemRenderable renderable) return renderable.ccg$getItemStack();
		else if (CCGUtil.getE() instanceof IItemRenderable renderable) return renderable.ccg$getItemStack();
		else return null;
	}
	/**
	 * 在屏幕中央区域渲染指定物品堆的图标及关联的悬浮提示信息。
	 *
	 * @param guiGraphics GUI渲染上下文对象，用于执行图形绘制操作
	 * @param itemStack   需要渲染的物品堆实例。若值为{@code null}或空物品堆叠时方法立即返回
	 */
	public static void renderItemStack(GuiGraphics guiGraphics, ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty()) return;
		var mc = Minecraft.getInstance();
		var font = mc.font;
		var flag = new Default(mc.options.advancedItemTooltips, true);
		var tooltip = itemStack.getTooltipLines(TooltipContext.of(mc.level), mc.player, flag);
		var cfg = AllConfigs.client();
		var tooltipTextWidth = tooltip.stream().mapToInt(mc.font::width).max().orElse(0) + 24;
		var x = guiGraphics.guiWidth() / 2 + cfg.overlayOffsetX.get();
		var y = guiGraphics.guiHeight() / 2 + cfg.overlayOffsetY.get();
		if (x + tooltipTextWidth > guiGraphics.guiWidth()) x = guiGraphics.guiWidth() - tooltipTextWidth;
		if (fade < 1) x += (int) (Math.pow(1 - fade, 3) * Math.signum(cfg.overlayOffsetX.get() + .5f) * 8);
		if (GoggleOverlayRenderer.hoverTicks != 0) y -= (tooltip.size() + 1) * 10;
		x = Math.max(16, x);
		y = Math.max(16, y);
		guiGraphics.renderItem(itemStack, x - 10, y - 10);
		guiGraphics.renderItemDecorations(font, itemStack, x - 10, y - 10);
		guiGraphics.renderTooltip(font, itemStack, x, y);
	}
}
