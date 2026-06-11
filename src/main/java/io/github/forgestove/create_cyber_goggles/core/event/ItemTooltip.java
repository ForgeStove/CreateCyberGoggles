package io.github.forgestove.create_cyber_goggles.core.event;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.equipment.armor.*;
import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import com.zurrtum.create.content.equipment.wrench.WrenchItem;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.TooltipOverlayRenderer;
import io.github.forgestove.create_cyber_goggles.core.gui.*;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public final class ItemTooltip {
	public static final List<TooltipOverlayRenderer> OVERLAY_RENDERERS = List.of(
		new ContainerRenderer(),
		new PackageItemRenderer(),
		new ToolboxRenderer(),
		new ListFilterRenderer(),
		new EnderChestRenderer(),
		new ClipboardRenderer(),
		new MapTooltipRenderer(),
		new LinkedControllerRenderer(),
		new TableClothRenderer(),
		new RedstoneRequesterRenderer()
	);
	private static final int OVERLAY_GAP = 6;
	/** Shared capture: set by mixins before deferred tooltip render */
	public static ItemStack capturedStack = ItemStack.EMPTY;
	public static int capturedMouseX, capturedMouseY;
	public static void itemTooltip(ItemStack stack, TooltipContext ignoredContext, TooltipFlag ignoredFlag, List<Component> tooltip) {
		if (!CCG.config.tooltip.extraItemTooltip) return;
		goggles(stack, tooltip);
		backtank(stack, tooltip);
		divingBoots(stack, tooltip);
		wrench(stack, tooltip);
		fluidContainer(stack, tooltip);
	}
	private static void goggles(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.goggles) return;
		if (!(stack.getItem() instanceof GogglesItem)) return;
		if (mc.player == null) return;
		var component = CCGLang.enabled(GogglesItem.isWearingGoggles(mc.player)).component();
		tooltip.add(1, component);
	}
	private static void backtank(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.backtank) return;
		if (!(stack.getItem() instanceof BacktankItem)) return;
		var component = CreateLang.translate("gui.goggles.fluid_container.capacity")
			.style(ChatFormatting.GRAY)
			.add(CCGLang.fraction(BacktankUtil.getAir(stack), BacktankUtil.maxAir(stack)).component())
			.component();
		tooltip.add(1, component);
	}
	private static void divingBoots(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.divingBoots) return;
		if (!(stack.getItem() instanceof DivingBootsItem)) return;
		var component = CCGLang.enabled(CCG.config.misc.allowDivingBoot).component();
		tooltip.add(1, component);
	}
	private static void wrench(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.wrench) return;
		if (!(stack.getItem() instanceof WrenchItem)) return;
		var component = CCGLang.translate("config.option.wrench.leftClickFastDismantle")
			.space()
			.enabled(CCG.config.wrench.leftClickFastDismantle)
			.component();
		tooltip.add(1, component);
	}
	private static void fluidContainer(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.fluidContainer) return;
		try (var inventory = FluidHelper.getFluidInventory(stack)) {
			if (inventory == null || inventory.isEmpty()) return;
			var entries = new ArrayList<FluidStack>();
			var capacities = new ArrayList<Integer>();
			for (var i = 0; i < inventory.size(); i++) {
				var fluid = inventory.getStack(i);
				if (fluid.isEmpty()) continue;
				entries.add(fluid.copy());
				capacities.add(inventory.getMaxAmount(fluid));
			}
			if (entries.isEmpty()) {
				entries.add(FluidStack.EMPTY);
				capacities.add(inventory.getMaxAmount(FluidStack.EMPTY));
			}
			for (var i = 0; i < entries.size(); i++) {
				var fluid = entries.get(i);
				var capacity = i < capacities.size() ? capacities.get(i) : Math.max(1, fluid.getAmount());
				CCGLang.fluidEntry(fluid, capacity).addTo(1, tooltip);
			}
		}
	}
	public static void renderTooltipOverlay(
		ItemStack stack,
		GuiGraphicsExtractor gui,
		Font font,
		List<ClientTooltipComponent> components,
		int mouseX,
		int mouseY,
		ClientTooltipPositioner positioner
	) {
		if (!CCG.config.tooltip.extraItemTooltip) return;
		if (stack.isEmpty()) return;
		TooltipOverlayRenderer renderer = null;
		for (var overlayRenderer : OVERLAY_RENDERERS) {
			if (!overlayRenderer.supports(stack)) continue;
			renderer = overlayRenderer;
			break;
		}
		if (renderer == null) return;
		if (!renderer.canRender(stack)) return;
		if (components.isEmpty()) return;
		int tooltipWidth = 0, tooltipHeight = 0;
		for (var component : components) {
			tooltipWidth = Math.max(tooltipWidth, component.getWidth(font));
			tooltipHeight += component.getHeight(font);
		}
		var overlayWidth = renderer.width(stack);
		var overlayHeight = renderer.height(stack);
		var pos = positioner.positionTooltip(gui.guiWidth(), gui.guiHeight(), mouseX, mouseY, tooltipWidth, tooltipHeight);
		var overlayX = Mth.clamp(pos.x(), 0, Math.max(0, gui.guiWidth() - overlayWidth));
		var overlayY = pos.y() - overlayHeight - OVERLAY_GAP;
		if (overlayY < 16) overlayY = 16;
		renderer.render(gui, stack, overlayX - 4, overlayY);
	}
}
