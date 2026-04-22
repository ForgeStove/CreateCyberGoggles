package io.github.forgestove.create_cyber_goggles.core.event;
import com.mojang.datafixers.util.Either;
import com.simibubi.create.content.equipment.armor.*;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.TooltipOverlayRenderer;
import io.github.forgestove.create_cyber_goggles.core.gui.*;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderTooltipEvent.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.fluids.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2ic;

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
		new RedstoneRequesterRenderer()
	);
	private static final int OVERLAY_GAP = 6;
	public static void itemTooltip(@NotNull ItemTooltipEvent event) {
		if (!CCG.config.tooltip.extraItemTooltip) return;
		var stack = event.getItemStack();
		var tooltip = event.getToolTip();
		goggles(stack, tooltip);
		backtank(stack, tooltip);
		divingBoots(stack, tooltip);
		wrench(stack, tooltip);
		fluidContainer(stack, tooltip);
	}
	public static void gatherComponents(@NotNull GatherComponents event) {
		var elements = event.getTooltipElements();
		for (var i = 0; i < elements.size(); i++) {
			var left = elements.get(i).left().orElse(null);
			if (!(left instanceof Component comp)) continue;
			var entry = TooltipComponentUtil.removeItemEntry(comp);
			if (entry != null) {
				elements.set(i, Either.right(entry));
				continue;
			}
			var fluid = TooltipComponentUtil.removeFluidEntry(comp);
			if (fluid != null) {
				elements.set(i, Either.right(fluid));
				continue;
			}
			var fluidList = TooltipComponentUtil.removeFluidList(comp);
			if (fluidList != null) {
				elements.set(i, Either.right(fluidList));
				continue;
			}
			var data = TooltipComponentUtil.removeItemList(comp);
			if (data != null) elements.set(i, Either.right(data));
		}
	}
	private static void goggles(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.goggles) return;
		if (!(stack.getItem() instanceof GogglesItem)) return;
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
		var component = CCGLang.builder()
			.translate("config.option.wrench.leftClickFastDismantle")
			.space()
			.add(CCGLang.enabled(CCG.config.wrench.leftClickFastDismantle))
			.component();
		tooltip.add(1, component);
	}
	private static void fluidContainer(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.fluidContainer) return;
		var handler = FluidUtil.getFluidHandler(stack).orElse(null);
		if (handler == null || handler.getTanks() == 0) return;
		var entries = new ArrayList<FluidStack>();
		var capacities = new ArrayList<Integer>();
		for (var i = 0; i < handler.getTanks(); i++) {
			var fluid = handler.getFluidInTank(i);
			if (fluid.isEmpty()) continue;
			entries.add(fluid.copy());
			capacities.add(handler.getTankCapacity(i));
		}
		if (entries.isEmpty()) {
			entries.add(FluidStack.EMPTY);
			capacities.add(handler.getTankCapacity(0));
		}
		for (var i = 0; i < entries.size(); i++) {
			var fluid = entries.get(i);
			var capacity = i < capacities.size() ? capacities.get(i) : Math.max(1, fluid.getAmount());
			CCGLang.fluidEntry(fluid, capacity).addTo(1, tooltip);
		}
	}
	public static void renderTooltipPre(@NotNull Pre event) {
		if (!CCG.config.tooltip.extraItemTooltip) return;
		var stack = event.getItemStack();
		TooltipOverlayRenderer renderer = null;
		for (var overlayRenderer : OVERLAY_RENDERERS) {
			if (!overlayRenderer.supports(stack)) continue;
			renderer = overlayRenderer;
			break;
		}
		if (renderer == null) return;
		if (!renderer.canRender(stack)) return;
		var font = event.getFont();
		var components = event.getComponents();
		if (components.isEmpty()) return;
		int tooltipWidth = 0, tooltipHeight = 0;
		for (var component : components) {
			tooltipWidth = Math.max(tooltipWidth, component.getWidth(font));
			tooltipHeight += component.getHeight();
		}
		var overlayWidth = renderer.width(stack);
		var overlayHeight = renderer.height(stack);
		var pos = getPos(event, tooltipWidth, tooltipHeight);
		var overlayX = getOverlayX(event, pos, overlayWidth);
		var overlayY = getOverlayY(pos, overlayHeight);
		if (overlayY < 0) {
			event.setY(event.getY() - overlayY);
			pos = getPos(event, tooltipWidth, tooltipHeight);
			overlayX = getOverlayX(event, pos, overlayWidth);
			overlayY = Math.max(0, getOverlayY(pos, overlayHeight));
		}
		renderer.render(event.getGraphics(), stack, overlayX - 4, overlayY);
	}
	private static int getOverlayX(@NotNull Pre event, Vector2ic pos, int overlayWidth) {
		return Mth.clamp(pos.x(), 0, Math.max(0, event.getScreenWidth() - overlayWidth));
	}
	private static int getOverlayY(Vector2ic pos, int overlayHeight) {
		return pos.y() - overlayHeight - OVERLAY_GAP;
	}
	private static @NotNull Vector2ic getPos(@NotNull Pre event, int width, int height) {
		return event.getTooltipPositioner()
			.positionTooltip(event.getScreenWidth(), event.getScreenHeight(), event.getX(), event.getY(), width, height);
	}
}
