package io.github.forgestove.create_cyber_goggles.core.event;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.gui.element.BoxElement;
import com.zurrtum.create.client.content.equipment.goggles.GoggleOverlayRenderer;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import io.github.forgestove.create_cyber_goggles.core.factory.*;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientFluidEntryTooltipComponent.FluidEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.TooltipTheme.Theme;
import io.github.forgestove.create_cyber_goggles.core.util.TooltipComponentUtil;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.*;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.*;
import org.joml.Vector2ic;
import org.jspecify.annotations.NonNull;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class TooltipOverlay {
	public static int hoverTicks;
	public static void renderOverlay(GuiGraphics gui, DeltaTracker ignoredDeltaTracker) {
		if (!CCG.config.overlay.renderItemOverlay || !CCG.config.gameMode.enableGoggles) return;
		if (mc.isPaused() || isInGUI() || mc.options.hideGui) {
			hoverTicks = 0;
			return;
		}
		if (!CCG.config.goggles.canRenderOnValueBox && hasActivedValueBox()) return;
		var itemStack = toRenderItemStack();
		if (itemStack.isEmpty()) hoverTicks = 0;
		else renderItemStack(gui, itemStack);
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
		var pose = gui.pose();
		pose.pushMatrix();
		var overlay = CCG.config.overlay;
		var cfg = AllConfigs.client();
		var theme = getTheme();
		var back = theme.backColor();
		var top = theme.topColor();
		var bot = theme.botColor();
		var fade = Mth.clamp((getRealtimeDeltaTicks() + hoverTicks++) / 24F, 0, 1);
		if (fade < 1) {
			pose.translate((float) (Math.pow(1 - fade, 3) * Math.signum(cfg.overlayOffsetX.get() + 0.5D) * 8), 0);
			back.scaleAlpha(fade);
			top.scaleAlpha(fade);
			bot.scaleAlpha(fade);
		}
		var width = gui.guiWidth();
		var height = gui.guiHeight();
		var x = width / 2 + cfg.overlayOffsetX.get() + overlay.overlayOffsetX;
		var y = height / 2 + cfg.overlayOffsetY.get() + overlay.overlayOffsetY;
		if (overlay.tooltipFlagType == null) overlay.tooltipFlagType = TooltipFlagType.Default;
		var tooltipLines = itemStack.getTooltipLines(TooltipContext.of(mc.level), mc.player, overlay.tooltipFlagType.getFlag());
		var components = buildTooltipComponents(tooltipLines, width - x - 16, true);
		components.set(0, new ClientItemEntryTooltipComponent(itemStack, 0, itemStack.getHoverName()));
		var tooltipWidth = 0;
		var tooltipHeight = 0;
		var font = mc.font;
		for (var component : components) {
			tooltipWidth = Math.max(tooltipWidth, component.getWidth(font));
			tooltipHeight += component.getHeight(font);
		}
		if (GoggleOverlayRenderer.hoverTicks != 0) y -= tooltipHeight + 10;
		x = Mth.clamp(x, 0, width - tooltipWidth);
		y = Mth.clamp(y, 16, height - tooltipHeight - 100);
		var tooltipPos = renderTooltip(gui, components, x, y, tooltipWidth, tooltipHeight, back.getRGB(), top.getRGB(), bot.getRGB());
		if (tooltipPos != null) renderTooltipOverlay(itemStack, gui, components, tooltipPos);
		pose.popMatrix();
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
	@Nullable
	public static Vector2ic renderTooltip(
		GuiGraphics gui,
		@NotNull List<ClientTooltipComponent> components,
		int x,
		int y,
		int tooltipWidth,
		int tooltipHeight,
		int back,
		int top,
		int bot
	) {
		if (components.isEmpty()) return null;
		var width = gui.guiWidth();
		var height = gui.guiHeight();
		var positioner = DefaultTooltipPositioner.INSTANCE;
		var tooltipPos = positioner.positionTooltip(width, height, x, y, tooltipWidth, tooltipHeight);
		var tooltipX = tooltipPos.x();
		var tooltipY = tooltipPos.y();
		var pose = gui.pose();
		pose.pushMatrix();
		renderTooltipBackground(gui, tooltipX, tooltipY, tooltipWidth, tooltipHeight, back, top, bot);
		int i = 0, textY = tooltipY;
		var font = mc.font;
		for (var component : components) {
			component.renderText(gui, font, tooltipX, textY);
			component.renderImage(font, tooltipX, textY, tooltipWidth, tooltipHeight, gui);
			textY += component.getHeight(font) + (i == 0 ? 2 : 0);
			i++;
		}
		pose.popMatrix();
		return tooltipPos;
	}
	public static void renderTooltipOverlay(
		ItemStack itemStack,
		GuiGraphics gui,
		List<ClientTooltipComponent> components,
		Vector2ic tooltipPos
	) {
		if (!CCG.config.tooltip.extraItemTooltip) return;
		if (itemStack.isEmpty()) return;
		TooltipOverlayRenderer renderer = null;
		for (var overlayRenderer : ItemTooltip.OVERLAY_RENDERERS) {
			if (!overlayRenderer.supports(itemStack)) continue;
			renderer = overlayRenderer;
			break;
		}
		if (renderer == null || !renderer.canRender(itemStack)) return;
		if (components.isEmpty()) return;
		var overlayWidth = renderer.width(itemStack);
		var overlayHeight = renderer.height(itemStack);
		var overlayX = Mth.clamp(tooltipPos.x(), 0, Math.max(0, gui.guiWidth() - overlayWidth));
		var overlayY = tooltipPos.y() - overlayHeight - 6;
		if (overlayY < 16) overlayY = 16;
		renderer.render(gui, itemStack, overlayX - 4, overlayY);
	}
	public static void renderTooltipBackground(@NonNull GuiGraphics gui, int x, int y, int width, int height, int back, int top, int bot) {
		gui.fillGradient(x - 3, y - 4, x + width + 3, y - 3, back, back);
		gui.fillGradient(x - 3, y + height + 3, x + width + 3, y + height + 4, back, back);
		gui.fillGradient(x - 3, y - 3, x + width + 3, y + height + 3, back, back);
		gui.fillGradient(x - 4, y - 3, x - 3, y + height + 3, back, back);
		gui.fillGradient(x + width + 3, y - 3, x + width + 4, y + height + 3, back, back);
		gui.fillGradient(x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, top, bot);
		gui.fillGradient(x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, top, bot);
		gui.fillGradient(x - 3, y - 3, x + width + 3, y - 3 + 1, top, top);
		gui.fillGradient(x - 3, y + height + 2, x + width + 3, y + height + 3, bot, bot);
	}
}
