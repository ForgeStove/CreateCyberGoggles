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
		currentItemStack = toRenderItemStack();
		if (currentItemStack.isEmpty()) hoverTicks = 0;
		else renderItemStack(guiGraphics, currentItemStack);
	}
	public static @NotNull ItemStack toRenderItemStack() {
		try {
			if (getBlockEntity() instanceof IItemRenderable renderable) return orEmpty(renderable.ccg$getItemStack());
			if (getEntity() instanceof IItemRenderable renderable) return orEmpty(renderable.ccg$getItemStack());
		} catch (Throwable e) {
			CCG.LOGGER.error("Failed to get item stack from IItemRenderable", e);
		}
		return ItemStack.EMPTY;
	}
	public static void renderItemStack(@NotNull GuiGraphics gui, @NotNull ItemStack itemStack) {
		var flag = new Default(mc.options.advancedItemTooltips, true);
		var tooltips = itemStack.getTooltipLines(mc.player, flag);
		tooltips.set(0, Component.literal(" ".repeat(Mth.ceil(16F / mc.font.width(" ")))).append(tooltips.get(0)));
		var tooltipTextWidth = tooltips.stream().mapToInt(mc.font::width).max().orElse(0) + 24;
		var overlay = CCG.CONFIG.overlay;
		var width = gui.guiWidth();
		var height = gui.guiHeight();
		var cfg = AllConfigs.client();
		var x = width / 2 + cfg.overlayOffsetX.get() + overlay.overlayOffsetX;
		var y = height / 2 + cfg.overlayOffsetY.get() + overlay.overlayOffsetY;
		if (GoggleOverlayRenderer.hoverTicks != 0) y -= (tooltips.size() + 1) * 10;
		x = Mth.clamp(x, 18, width - tooltipTextWidth);
		y = Mth.clamp(y, 18, height);
		var useCCGCustom = overlay.useCustomColor;
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
		var poseStack = gui.pose();
		poseStack.pushPose();
		var fade = Mth.clamp((hoverTicks++ + mc.getFrameTime()) / 24F, 0, 1);
		if (fade < 1) {
			poseStack.translate(Math.pow(1 - fade, 3) * Math.signum(cfg.overlayOffsetX.get() + .5F) * 8, 0, 0);
			back.scaleAlpha(fade);
			top.scaleAlpha(fade);
			bot.scaleAlpha(fade);
		}
		RemovedGuiUtils.drawHoveringText(gui, tooltips, x, y, width, height, -1, back.getRGB(), top.getRGB(), bot.getRGB(), mc.font);
		var scale = 0.75F;
		var itemX = (int) ((x + 12) / scale);
		var itemY = (int) ((y - 14) / scale);
		poseStack.scale(scale, scale, 1F);
		GuiGameElement.of(itemStack).at(itemX, itemY, 450).render(gui);
		gui.renderItemDecorations(mc.font, itemStack, itemX, itemY);
		poseStack.popPose();
	}
}
