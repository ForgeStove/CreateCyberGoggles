package io.github.forgestove.create_cyber_goggles.content.goggles.tooltip;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.simibubi.create.*;
import com.simibubi.create.content.equipment.armor.*;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.CCGConfig.Schematic.Preview;
import io.github.forgestove.create_cyber_goggles.api.*;
import io.github.forgestove.create_cyber_goggles.compat.simulated.NavigationDistanceHelper;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.TooltipComponentUtil.MarkerSplit;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.component.ClientFluidEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.component.ClientFluidEntryTooltipComponent.FluidEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.component.ClientSchematicPreviewTooltipComponent.SchematicPreviewTooltip;
import io.github.forgestove.create_cyber_goggles.foundation.events.CCGKey;
import io.github.forgestove.create_cyber_goggles.foundation.lang.*;
import io.github.forgestove.create_cyber_goggles.foundation.util.CCGMods;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.network.chat.*;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RenderTooltipEvent.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.*;
import org.joml.Vector2ic;
import org.slf4j.Logger;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.foundation.util.CCGUtil.*;
public class ItemTooltip {
	public final static List<TooltipRenderer> OVERLAY_RENDERERS = new ArrayList<>();
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int PREVIEW_MIN_SIZE = 48;
	private static final int PREVIEW_EDGE_PADDING = 16;
	private static final int PREVIEW_TEXT_HEIGHT = 48;
	private static final int PREVIEW_TOOLTIP_INDEX = 1;
	static {
		String annoName = AutoTooltipRenderer.class.getName();
		ModList.get().getAllScanData().forEach(scanData -> scanData.getAnnotations().forEach(annoData -> {
			if (annoName.equals(annoData.annotationType().getClassName())) try {
				Class<?> clazz = Class.forName(annoData.memberName());
				if (TooltipRenderer.class.isAssignableFrom(clazz))
					OVERLAY_RENDERERS.add((TooltipRenderer) clazz.getDeclaredConstructor().newInstance());
			} catch (Exception e) {
				LOGGER.error("Unable to load tooltip renderer: {}", annoData.memberName(), e);
			}
		}));
	}
	public static void itemTooltip(@NotNull ItemTooltipEvent event) {
		if (!CCG.config.tooltip.extraItemTooltip) return;
		if (shouldSuppressInfo()) return;
		ItemStack stack = event.getItemStack();
		List<Component> tooltip = event.getToolTip();
		goggles(stack, tooltip);
		backtank(stack, tooltip);
		divingBoots(stack, tooltip);
		wrench(stack, tooltip);
		fluidContainer(stack, tooltip);
		navigationDistance(stack, tooltip);
		schematicPreview(stack, tooltip);
	}
	private static void goggles(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.goggles) return;
		if (!(stack.getItem() instanceof GogglesItem)) return;
		if (mc.player == null) return;
		CCGLang.of().enabled(GogglesItem.isWearingGoggles(mc.player)).addTo(1, tooltip);
	}
	private static void backtank(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.backtank) return;
		if (!(stack.getItem() instanceof BacktankItem)) return;
		CCGLang.of()
			.add(CCGCreateLang.translateDirect("gui.goggles.fluid_container.capacity").withStyle(ChatFormatting.GRAY))
			.fraction(BacktankUtil.getAir(stack), BacktankUtil.maxAir(stack))
			.addTo(1, tooltip);
	}
	private static void divingBoots(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.divingBoots) return;
		if (!(stack.getItem() instanceof DivingBootsItem)) return;
		CCGLang.of().enabled(CCG.config.misc.allowDivingBoot).addTo(1, tooltip);
	}
	private static void wrench(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.wrench) return;
		if (!(stack.getItem() instanceof WrenchItem)) return;
		CCGLang.of()
			.translate("config.option.misc.wrench.leftClickFastDismantle")
			.space()
			.enabled(CCG.config.misc.wrench.leftClickFastDismantle)
			.addTo(1, tooltip);
	}
	private static void fluidContainer(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.fluidContainer) return;
		IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack).orElse(null);
		if (handler == null || handler.getTanks() == 0) return;
		var entries = new ArrayList<FluidStack>();
		var capacities = new ArrayList<Integer>();
		for (var i = 0; i < handler.getTanks(); i++) {
			FluidStack fluid = handler.getFluidInTank(i);
			if (fluid.isEmpty()) continue;
			entries.add(fluid.copy());
			capacities.add(handler.getTankCapacity(i));
		}
		if (entries.isEmpty()) {
			entries.add(FluidStack.EMPTY);
			capacities.add(handler.getTankCapacity(0));
		}
		for (var i = 0; i < entries.size(); i++) {
			FluidStack fluid = entries.get(i);
			int capacity = i < capacities.size() ? capacities.get(i) : Math.max(1, fluid.getAmount());
			CCGLang.of().fluidEntry(fluid, capacity).addTo(1, tooltip);
		}
	}
	private static void navigationDistance(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.aeronautics.navigationDistance) return;
		CCGMods.simulated.runIfInstalled(() -> NavigationDistanceHelper.line(stack)).ifPresent(line -> tooltip.add(1, line));
	}
	private static void schematicPreview(@NotNull ItemStack stack, List<Component> tooltip) {
		Preview preview = CCG.config.schematic.preview;
		if (!preview.previewEnabled || !preview.tooltipPreview) return;
		if (!AllItems.SCHEMATIC.isIn(stack) || !stack.has(AllDataComponents.SCHEMATIC_FILE)) return;
		String fileName = stack.get(AllDataComponents.SCHEMATIC_FILE);
		if (fileName == null || fileName.isBlank()) return;
		boolean alt = Screen.hasAltDown();
		int index = Math.min(PREVIEW_TOOLTIP_INDEX, tooltip.size());
		tooltip.add(index, CCGKey.hint(CCGLang.translateDirect("gui.schematicPreview.altHint", CCGKey.keyName("Alt", alt))));
		if (!alt) return;
		MutableComponent marker = Component.empty();
		int width = Mth.clamp(mc.getWindow().getGuiScaledWidth() - PREVIEW_EDGE_PADDING, PREVIEW_MIN_SIZE, preview.sidePanelWidth);
		int height = Mth.clamp(mc.getWindow().getGuiScaledHeight() - PREVIEW_TEXT_HEIGHT, PREVIEW_MIN_SIZE, preview.maxHeight);
		TooltipComponentUtil.SCHEMATIC_PREVIEW_MAP.put(marker, new SchematicPreviewTooltip(fileName, width, height));
		tooltip.add(index + 1, marker);
	}
	public static void gatherComponents(@NotNull GatherComponents event) {
		List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
		var sharedBarWidth = 0;
		for (Either<FormattedText, TooltipComponent> element : elements) {
			if (!(element.left().orElse(null) instanceof Component comp)) continue;
			FluidEntryTooltipComponent fluid = TooltipComponentUtil.peekFluidEntry(comp);
			if (fluid == null) continue;
			int preferred = ClientFluidEntryTooltipComponent.preferredBarWidth(mc.font, fluid.fluid(), fluid.capacityMb(), fluid.label());
			if (preferred > sharedBarWidth) sharedBarWidth = preferred;
		}
		for (var i = 0; i < elements.size(); ) {
			FormattedText left = elements.get(i).left().orElse(null);
			if (!(left instanceof Component comp)) {
				i++;
				continue;
			}
			MarkerSplit split = TooltipComponentUtil.consumeMarker(comp);
			if (split == null) {
				i++;
				continue;
			}
			TooltipComponent ui = split.data();
			if (ui instanceof FluidEntryTooltipComponent fluid)
				ui = new FluidEntryTooltipComponent(fluid.fluid(), fluid.indent(), fluid.capacityMb(), sharedBarWidth, fluid.label());
			if (split.remaining() != null) {
				elements.set(i, Either.left(split.remaining()));
				elements.add(i + 1, Either.right(ui));
			} else elements.set(i, Either.right(ui));
			i++;
		}
	}
	public static void renderTooltipPre(@NotNull Pre event) {
		if (!CCG.config.tooltip.extraItemTooltip) return;
		ItemStack stack = event.getItemStack();
		TooltipRenderer renderer = findRenderer(stack);
		if (renderer == null) return;
		renderer.begin(stack);
		try {
			renderOverlay(event, stack, renderer);
		} finally {
			renderer.end();
		}
	}
	private static void renderOverlay(@NotNull Pre event, ItemStack stack, TooltipRenderer renderer) {
		if (!renderer.canRender(stack)) return;
		Font font = event.getFont();
		List<ClientTooltipComponent> components = event.getComponents();
		if (components.isEmpty()) return;
		int tooltipWidth = 0, tooltipHeight = 0;
		for (ClientTooltipComponent component : components) {
			tooltipWidth = Math.max(tooltipWidth, component.getWidth(font));
			tooltipHeight += component.getHeight();
		}
		int overlayWidth = renderer.width(stack);
		int overlayHeight = renderer.height(stack);
		ClientTooltipPositioner positioner = event.getTooltipPositioner();
		int screenWidth = event.getScreenWidth();
		int screenHeight = event.getScreenHeight();
		Vector2ic pos = positioner.positionTooltip(screenWidth, screenHeight, event.getX(), event.getY(), tooltipWidth, tooltipHeight);
		int minTop = overlayHeight + 6;
		if (pos.y() < minTop) {
			int low = event.getY();
			int high = screenHeight;
			while (low < high) {
				int mid = low + high >>> 1;
				Vector2ic test = positioner.positionTooltip(screenWidth, screenHeight, event.getX(), mid, tooltipWidth, tooltipHeight);
				if (test.y() >= minTop) high = mid;
				else low = mid + 1;
			}
			if (low > event.getY()) {
				event.setY(low);
				pos = positioner.positionTooltip(screenWidth, screenHeight, event.getX(), low, tooltipWidth, tooltipHeight);
			}
		}
		int overlayX = Mth.clamp(pos.x(), 0, Math.max(0, screenWidth - overlayWidth));
		int overlayY = Mth.clamp(pos.y() - overlayHeight - 6, 0, Math.max(0, screenHeight - overlayHeight));
		GuiGraphics gui = event.getGraphics();
		PoseStack pose = gui.pose();
		pose.pushPose();
		renderer.render(gui, stack, overlayX - 4, overlayY);
		// 登记已渲染区域，供后渲染的 GoggleOverlay 避让
		OverlayManager.upperBottom = Math.max(OverlayManager.upperBottom, overlayY + overlayHeight);
		pose.popPose();
	}
	public static @Nullable TooltipRenderer findRenderer(@NotNull ItemStack stack) {
		for (TooltipRenderer renderer : OVERLAY_RENDERERS) if (renderer.supports(stack)) return renderer;
		return null;
	}
}
