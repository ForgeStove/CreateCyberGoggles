package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import com.simibubi.create.foundation.gui.RemovedGuiUtils;
import com.simibubi.create.infrastructure.config.AllConfigs;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.createmod.catnip.gui.element.*;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.*;
import org.jetbrains.annotations.NotNull;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class Overlay {
	public static int hoverTicks;
	public static float fade;
	@NotNull public static ItemStack currentItemStack = ItemStack.EMPTY;
	public static void register(@NotNull RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "goggle_overlay", Overlay::renderOverlay);
	}
	public static void renderOverlay(ForgeGui gui, GuiGraphics guiGraphics, float partialTicks, int width, int height) {
		if (!CCG.CONFIG.overlay.renderExtraItems || !CCG.CONFIG.gameMode.enableGoggles) return;
		if (mc.isPaused() || isInGUI() || mc.options.hideGui) {
			currentItemStack = ItemStack.EMPTY;
			hoverTicks = 0;
			return;
		}
		if (!CCG.CONFIG.goggles.canRenderOnValueBox && hasActivedValueBox()) return;
		fade = Mth.clamp((hoverTicks++ + partialTicks) / 24f, 0, 1);
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
	 * @param gui       GUI渲染上下文对象，用于执行图形绘制操作
	 * @param itemStack 需要渲染的物品堆实例。若值为{@link ItemStack#EMPTY}时方法立即返回
	 */
	public static void renderItemStack(GuiGraphics gui, @NotNull ItemStack itemStack) {
		if (itemStack.isEmpty()) return;
		var flag = new Default(mc.options.advancedItemTooltips, true);
		var tooltip = itemStack.getTooltipLines(mc.player, flag)
			.stream()
			.map(line -> line.getString().isBlank() ? line : Component.literal("    ").append(line))
			.toList();
		var tooltipTextWidth = tooltip.stream().mapToInt(mc.font::width).max().orElse(0) + 24;
		var overlay = CCG.CONFIG.overlay;
		var width = gui.guiWidth();
		var height = gui.guiHeight();
		var x = width / 2 + AllConfigs.client().overlayOffsetX.get() + overlay.overlayOffsetX;
		var y = height / 2 + AllConfigs.client().overlayOffsetY.get() + overlay.overlayOffsetY;
		if (x + tooltipTextWidth > width) x = width - tooltipTextWidth;
		if (fade < 1) x += (int) (Math.pow(1 - fade, 3) * Math.signum(AllConfigs.client().overlayOffsetX.get() + .5f) * 8);
		if (GoggleOverlayRenderer.hoverTicks != 0) y -= (tooltip.size() + 1) * 10;
		x = Math.max(16, x);
		y = Math.max(16, y);
		var useCCGCustom = overlay.useCustomColor;
		var cfg = AllConfigs.client();
		var useCreateCustom = cfg.overlayCustomColor.get();
		var back = useCCGCustom
			? new Color(overlay.backgroundColor)
			: useCreateCustom ? new Color(cfg.overlayBackgroundColor.get()) : BoxElement.COLOR_VANILLA_BACKGROUND.scaleAlpha(.75f);
		var top = useCCGCustom
			? new Color(overlay.borderTopColor)
			: useCreateCustom ? new Color(cfg.overlayBorderColorTop.get()) : BoxElement.COLOR_VANILLA_BORDER.getFirst().copy();
		var bot = useCCGCustom
			? new Color(overlay.borderBottomColor)
			: useCreateCustom ? new Color(cfg.overlayBorderColorBot.get()) : BoxElement.COLOR_VANILLA_BORDER.getSecond().copy();
		if (fade < 1) {
			back.scaleAlpha(fade);
			top.scaleAlpha(fade);
			bot.scaleAlpha(fade);
		}
		RemovedGuiUtils.drawHoveringText(gui, tooltip, x, y, width, height, -1, back.getRGB(), top.getRGB(), bot.getRGB(), mc.font);
		var itemX = x + 10;
		var itemY = y - 16;
		GuiGameElement.of(itemStack).at(itemX, itemY, 450).render(gui);
		gui.renderItemDecorations(mc.font, itemStack, itemX, itemY);
	}
}
