package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import com.simibubi.create.infrastructure.config.AllConfigs;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class Overlay {
	public static int hoverTicks;
	@NotNull public static ItemStack currentItemStack = ItemStack.EMPTY;
	public static void register(@NotNull RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "item_tooltip_overlay", Overlay::renderOverlay);
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
		var width = gui.guiWidth();
		var height = gui.guiHeight();
		var overlay = CCG.CONFIG.overlay;
		var cfg = AllConfigs.client();
		var x = width / 2 + cfg.overlayOffsetX.get() + overlay.overlayOffsetX;
		var y = height / 2 + cfg.overlayOffsetY.get() + overlay.overlayOffsetY;
		var flag = new Default(mc.options.advancedItemTooltips, true);
		var tooltipsRaw = itemStack.getTooltipLines(mc.player, flag);
		tooltipsRaw.set(0, Component.literal(" ".repeat(Mth.ceil(16F / mc.font.width(" ")))).append(tooltipsRaw.get(0)));
		final var finalX = x;
		var tooltips = tooltipsRaw.stream().flatMap(line -> mc.font.split(line, width - finalX).stream()).toList();
		var tooltipWidth = tooltips.stream().mapToInt(mc.font::width).max().orElse(0) + 16;
		var tooltipHeight = (tooltips.size() + 1) * 10;
		var useCCGCustom = overlay.useCustomColor;
		var useCreateCustom = cfg.overlayCustomColor.get();
		var back = useCCGCustom
			? new Color(overlay.backgroundColor)
			: useCreateCustom ? new Color(cfg.overlayBackgroundColor.get()) : BoxElement.COLOR_VANILLA_BACKGROUND.scaleAlpha(0.75F);
		var top = useCCGCustom
			? new Color(overlay.borderTopColor)
			: useCreateCustom ? new Color(cfg.overlayBorderColorTop.get()) : BoxElement.COLOR_VANILLA_BORDER.getFirst().copy();
		var bot = useCCGCustom
			? new Color(overlay.borderBottomColor)
			: useCreateCustom ? new Color(cfg.overlayBorderColorBot.get()) : BoxElement.COLOR_VANILLA_BORDER.getSecond().copy();
		if (GoggleOverlayRenderer.hoverTicks != 0) y -= tooltipHeight;
		x = Mth.clamp(x, 16, width - tooltipWidth);
		y = Mth.clamp(y, 16, height - tooltipHeight);
		var pose = gui.pose();
		pose.pushPose();
		var fade = Mth.clamp((hoverTicks++ + mc.getFrameTime()) / 24F, 0, 1);
		if (fade < 1) {
			pose.translate(Math.pow(1 - fade, 3) * Math.signum(cfg.overlayOffsetX.get() + .5F) * 8, 0, 0);
			back.scaleAlpha(fade);
			top.scaleAlpha(fade);
			bot.scaleAlpha(fade);
		}
		var components = tooltips.stream().map(ClientTooltipComponent::create).collect(Collectors.toList());
		renderTooltip(gui, itemStack, components, x, y, back.getRGB(), top.getRGB(), bot.getRGB());
		pose.translate(x + 12, y - 14, 450);
		pose.scale(0.75F, 0.75F, 1F);
		gui.renderItem(itemStack, 0, 0);
		gui.renderItemDecorations(mc.font, itemStack, 0, 0);
		pose.popPose();
	}
	private static void renderTooltip(
		GuiGraphics gui,
		ItemStack itemStack,
		@NotNull List<ClientTooltipComponent> components,
		int x,
		int y,
		int back,
		int top,
		int bot
	) {
		if (components.isEmpty()) return;
		var pose = gui.pose();
		var width = gui.guiWidth();
		var height = gui.guiHeight();
		var positioner = DefaultTooltipPositioner.INSTANCE;
		//noinspection UnstableApiUsage
		if (ForgeHooksClient.onRenderTooltipPre(itemStack, gui, x, y, width, height, components, mc.font, positioner).isCanceled()) return;
		var maxWidth = 0;
		var totalHeight = components.size() == 1 ? -2 : 0;
		for (var clientTooltipComponent : components) {
			var componentWidth = clientTooltipComponent.getWidth(mc.font);
			if (componentWidth > maxWidth) maxWidth = componentWidth;
			totalHeight += clientTooltipComponent.getHeight();
		}
		var tooltipPosition = positioner.positionTooltip(width, height, x, y, maxWidth, totalHeight);
		var tooltipX = tooltipPosition.x();
		var tooltipY = tooltipPosition.y();
		pose.pushPose();
		TooltipRenderUtil.renderTooltipBackground(gui, tooltipX, tooltipY, maxWidth, totalHeight, 400, back, back, top, bot);
		pose.translate(0, 0, 400);
		var textY = tooltipY;
		for (var i = 0; i < components.size(); i++) {
			var component = components.get(i);
			component.renderText(mc.font, tooltipX, textY, pose.last().pose(), gui.bufferSource());
			textY += component.getHeight() + (i == 0 ? 2 : 0);
		}
		textY = tooltipY;
		for (var i = 0; i < components.size(); i++) {
			var component = components.get(i);
			component.renderImage(mc.font, tooltipX, textY, gui);
			textY += component.getHeight() + (i == 0 ? 2 : 0);
		}
		pose.popPose();
	}
}
