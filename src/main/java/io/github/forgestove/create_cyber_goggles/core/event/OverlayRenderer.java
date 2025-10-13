package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import com.simibubi.create.infrastructure.config.AllConfigs;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.NotNull;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class OverlayRenderer {
	public static int hoverTicks;
	public static float fade;
	@NotNull public static ItemStack currentItemStack = ItemStack.EMPTY;
	public static void register(@NotNull RegisterGuiLayersEvent event) {
		event.registerAbove(
			VanillaGuiLayers.HOTBAR,
			ResourceLocation.fromNamespaceAndPath(CCG.ID, "item_tooltip_overlay"),
			OverlayRenderer::renderOverlay
		);
	}
	public static void color(RenderTooltipEvent.@NotNull Color event) {
		if (currentItemStack != event.getItemStack() || currentItemStack.isEmpty()) return;
		var colorBackground = AllConfigs.client().overlayCustomColor.get()
			? new Color(AllConfigs.client().overlayBackgroundColor.get())
			: BoxElement.COLOR_VANILLA_BACKGROUND.scaleAlpha(.75f);
		if (fade < 1) colorBackground.scaleAlpha(fade);
		event.setBackground(colorBackground.getRGB());
	}
	public static void renderOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if (!CCG.CONFIG.goggles.renderExtraItems || !CCG.CONFIG.gameMode.enableGoggle) return;
		if (mc.isPaused() || isInGUI() || mc.options.hideGui) {
			currentItemStack = ItemStack.EMPTY;
			hoverTicks = 0;
			return;
		}
		if (!CCG.CONFIG.goggles.canRenderOnValueBox && hasActivedValueBox()) return;
		fade = Mth.clamp((hoverTicks++ + deltaTracker.getGameTimeDeltaPartialTick(false)) / 24f, 0, 1);
		currentItemStack = toRenderItemStack();
		if (currentItemStack.isEmpty()) hoverTicks = 0;
		else renderItemStack(guiGraphics, currentItemStack);
	}
	public static @NotNull ItemStack toRenderItemStack() {
		try {
			if (getBlockEntity() instanceof IItemRenderable renderable) return orEmpty(renderable.ccg$getItemStack());
			if (getEntity() instanceof IItemRenderable renderable) return orEmpty(renderable.ccg$getItemStack());
		} catch (Exception exception) {
			CCG.LOGGER.error("Failed to get item stack from IItemRenderable", exception);
		}
		return ItemStack.EMPTY;
	}
	/**
	 * 在屏幕中央区域渲染指定物品堆的图标及关联的悬浮提示信息。
	 *
	 * @param guiGraphics GUI渲染上下文对象，用于执行图形绘制操作
	 * @param itemStack   需要渲染的物品堆实例。若值为{@link ItemStack#EMPTY}时方法立即返回
	 */
	public static void renderItemStack(GuiGraphics guiGraphics, @NotNull ItemStack itemStack) {
		if (itemStack.isEmpty()) return;
		var flag = new Default(mc.options.advancedItemTooltips, true);
		var tooltip = itemStack.getTooltipLines(TooltipContext.of(mc.level), mc.player, flag);
		var tooltipTextWidth = tooltip.stream().mapToInt(mc.font::width).max().orElse(0) + 24;
		var x = guiGraphics.guiWidth() / 2 + AllConfigs.client().overlayOffsetX.get() + CCG.CONFIG.goggles.overlayOffsetX;
		var y = guiGraphics.guiHeight() / 2 + AllConfigs.client().overlayOffsetY.get() + CCG.CONFIG.goggles.overlayOffsetY;
		if (x + tooltipTextWidth > guiGraphics.guiWidth()) x = guiGraphics.guiWidth() - tooltipTextWidth;
		if (fade < 1) x += (int) (Math.pow(1 - fade, 3) * Math.signum(AllConfigs.client().overlayOffsetX.get() + .5f) * 8);
		if (GoggleOverlayRenderer.hoverTicks != 0) y -= (tooltip.size() + 1) * 10;
		x = Math.max(16, x);
		y = Math.max(16, y);
		var itemX = x - 10;
		var itemY = y - 10;
		guiGraphics.renderItem(itemStack, itemX, itemY);
		guiGraphics.renderItemDecorations(mc.font, itemStack, itemX, itemY);
		guiGraphics.renderTooltip(mc.font, itemStack, x, y);
	}
}
