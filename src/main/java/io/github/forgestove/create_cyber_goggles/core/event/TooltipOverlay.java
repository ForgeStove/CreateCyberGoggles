package io.github.forgestove.create_cyber_goggles.core.event;
import com.zurrtum.create.client.content.equipment.goggles.GoggleOverlayRenderer;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.ItemRenderable;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class TooltipOverlay {
	public static int hoverTicks;
	public static void renderOverlay(GuiGraphics graphics, DeltaTracker ignoredDeltaTracker) {
		if (!CCG.CONFIG.overlay.renderItemOverlay || !CCG.CONFIG.gameMode.enableGoggles) return;
		if (mc.isPaused() || isInGUI() || mc.options.hideGui) {
			hoverTicks = 0;
			return;
		}
		if (!CCG.CONFIG.goggles.canRenderOnValueBox && hasActivedValueBox()) return;
		var itemStack = toRenderItemStack();
		if (itemStack.isEmpty()) hoverTicks = 0;
		else renderItemStack(graphics, itemStack);
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
	/**
	 * 在屏幕中央区域渲染指定物品堆的图标及关联的悬浮提示信息。
	 *
	 * @param guiGraphics GUI渲染上下文对象，用于执行图形绘制操作
	 * @param itemStack   需要渲染的物品堆实例。若值为{@code null}或空物品堆叠时方法立即返回
	 */
	public static void renderItemStack(GuiGraphics guiGraphics, ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty()) return;
		var font = mc.font;
		var overlay = CCG.CONFIG.overlay;
		var flag = overlay.tooltipFlagType != null
			? overlay.tooltipFlagType.getFlag()
			: new TooltipFlag.Default(mc.options.advancedItemTooltips, false);
		var tooltip = itemStack.getTooltipLines(TooltipContext.of(mc.level), mc.player, flag);
		var cfg = AllConfigs.client();
		var fade = Mth.clamp((getRealtimeDeltaTicks() + hoverTicks++) / 24F, 0, 1);
		var tooltipTextWidth = tooltip.stream().mapToInt(mc.font::width).max().orElse(0) + 24;
		var x = guiGraphics.guiWidth() / 2 + cfg.overlayOffsetX.get() + overlay.overlayOffsetX;
		var y = guiGraphics.guiHeight() / 2 + cfg.overlayOffsetY.get() + overlay.overlayOffsetY;
		if (x + tooltipTextWidth > guiGraphics.guiWidth()) x = guiGraphics.guiWidth() - tooltipTextWidth;
		if (fade < 1) x += (int) (Math.pow(1 - fade, 3) * Math.signum(cfg.overlayOffsetX.get() + .5f) * 8);
		if (GoggleOverlayRenderer.hoverTicks != 0) y -= (tooltip.size() + 1) * 10;
		x = Math.max(16, x);
		y = Math.max(16, y);
		guiGraphics.renderItem(itemStack, x - 10, y - 10);
		guiGraphics.renderItemDecorations(font, itemStack, x - 10, y - 10);
		guiGraphics.renderTooltip(
			mc.font,
			tooltip.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList(),
			x,
			y,
			DefaultTooltipPositioner.INSTANCE,
			null
		);
	}
}
