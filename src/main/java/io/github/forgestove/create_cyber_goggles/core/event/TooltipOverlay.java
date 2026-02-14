package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import com.simibubi.create.infrastructure.config.AllConfigs;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import io.github.forgestove.create_cyber_goggles.core.util.TooltipTheme.Theme;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.*;
import org.jetbrains.annotations.*;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class TooltipOverlay {
	public static int hoverTicks;
	public static void register(@NotNull RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "item_tooltip_overlay", TooltipOverlay::renderOverlay);
	}
	public static void renderOverlay(ForgeGui gui, GuiGraphics graphics, float partialTicks, int width, int height) {
		if (!CCG.config.overlay.renderItemOverlay || !CCG.config.gameMode.enableGoggles) return;
		if (mc.isPaused() || isInGUI() || mc.options.hideGui) {
			hoverTicks = 0;
			return;
		}
		if (!CCG.config.goggles.canRenderOnValueBox && hasActivedValueBox()) return;
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
	public static void renderItemStack(@NotNull GuiGraphics graphics, @NotNull ItemStack itemStack) {
		var pose = graphics.pose();
		pose.pushPose();
		var overlay = CCG.config.overlay;
		var cfg = AllConfigs.client();
		var theme = getTheme();
		var back = theme.backColor();
		var top = theme.topColor();
		var bot = theme.botColor();
		var fade = Mth.clamp((getRealtimeDeltaTicks() + hoverTicks++) / 24F, 0, 1);
		if (fade < 1) {
			pose.translate(Math.pow(1 - fade, 3) * Math.signum(cfg.overlayOffsetX.get() + 0.5D) * 8, 0, 0);
			back.scaleAlpha(fade);
			top.scaleAlpha(fade);
			bot.scaleAlpha(fade);
		}
		var width = graphics.guiWidth();
		var height = graphics.guiHeight();
		var x = width / 2 + cfg.overlayOffsetX.get() + overlay.overlayOffsetX;
		var y = height / 2 + cfg.overlayOffsetY.get() + overlay.overlayOffsetY;
		var tooltips = getFormattedTooltips(itemStack, width - x - 16);
		var tooltipWidth = tooltips.stream().mapToInt(mc.font::width).max().orElse(0);
		var tooltipHeight = tooltips.size() * 10;
		if (GoggleOverlayRenderer.hoverTicks != 0) y -= tooltipHeight + 10;
		x = Mth.clamp(x, 0, width - tooltipWidth);
		y = Mth.clamp(y, 16, height - tooltipHeight - 100);
		renderTooltip(graphics, itemStack, tooltips, x, y, tooltipWidth, tooltipHeight, back.getRGB(), top.getRGB(), bot.getRGB());
		pose.translate(x + 14F, y - 14F, 450F);
		pose.scale(0.75F, 0.75F, 1F);
		graphics.renderItem(itemStack, 0, 0);
		graphics.renderItemDecorations(mc.font, itemStack, 0, 0);
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
			var back = useCreateCustom ? new Color(cfg.overlayBackgroundColor.get()) :
				BoxElement.COLOR_VANILLA_BACKGROUND.scaleAlpha(0.75F);
			var top = useCreateCustom ? new Color(cfg.overlayBorderColorTop.get()) : BoxElement.COLOR_VANILLA_BORDER.getFirst().copy();
			var bot = useCreateCustom ? new Color(cfg.overlayBorderColorBot.get()) : BoxElement.COLOR_VANILLA_BORDER.getSecond().copy();
			return new Theme(back, top, bot);
		}
		return new Theme(overlay.backgroundColor, overlay.borderTopColor, overlay.borderBottomColor);
	}
	public static @NotNull @Unmodifiable List<FormattedCharSequence> getFormattedTooltips(@NotNull ItemStack itemStack, int maxWidth) {
		var overlay = CCG.config.overlay;
		if (overlay.tooltipFlagType == null) overlay.tooltipFlagType = TooltipFlagType.Default;
		var tooltipLines = itemStack.getTooltipLines(mc.player, overlay.tooltipFlagType.getFlag());
		tooltipLines.set(0, Component.literal(" ".repeat(Mth.ceil(16F / mc.font.width(" ")))).append(tooltipLines.get(0)));
		return tooltipLines.stream().flatMap(line -> mc.font.split(line, maxWidth).stream()).toList();
	}
	public static void renderTooltip(
		GuiGraphics graphics,
		ItemStack itemStack,
		@NotNull List<FormattedCharSequence> tooltips,
		int x,
		int y,
		int tooltipWidth,
		int tooltipHeight,
		int back,
		int top,
		int bot
	) {
		var components = tooltips.stream().map(ClientTooltipComponent::create).toList();
		if (components.isEmpty()) return;
		var width = graphics.guiWidth();
		var height = graphics.guiHeight();
		var positioner = DefaultTooltipPositioner.INSTANCE;
		//noinspection UnstableApiUsage
		if (ForgeHooksClient.onRenderTooltipPre(itemStack, graphics, x, y, width, height, components, mc.font, positioner).isCanceled())
			return;
		var tooltipPos = positioner.positionTooltip(width, height, x, y, tooltipWidth, tooltipHeight);
		var tooltipX = tooltipPos.x();
		var tooltipY = tooltipPos.y();
		var pose = graphics.pose();
		pose.pushPose();
		TooltipRenderUtil.renderTooltipBackground(graphics, tooltipX, tooltipY, tooltipWidth, tooltipHeight, 400, back, back, top, bot);
		pose.translate(0, 0, 400);
		int i = 0, textY = tooltipY;
		for (var component : components) {
			component.renderText(mc.font, tooltipX, textY, pose.last().pose(), graphics.bufferSource());
			component.renderImage(mc.font, tooltipX, textY, graphics);
			textY += component.getHeight() + (i == 0 ? 2 : 0);
			i++;
		}
		pose.popPose();
	}
}
