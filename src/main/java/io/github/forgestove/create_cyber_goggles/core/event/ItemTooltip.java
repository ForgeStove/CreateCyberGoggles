package io.github.forgestove.create_cyber_goggles.core.event;
import com.mojang.datafixers.util.Either;
import com.simibubi.create.content.equipment.armor.*;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.*;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientFluidEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.factory.ClientFluidEntryTooltipComponent.FluidEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RenderTooltipEvent.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.fluids.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public final class ItemTooltip {
	public final static List<TooltipRenderer> OVERLAY_RENDERERS = new ArrayList<>();
	static {
		var annoName = AutoTooltipRenderer.class.getName();
		ModList.get().getAllScanData().forEach(scanData -> scanData.getAnnotations().forEach(annoData -> {
			if (annoName.equals(annoData.annotationType().getClassName())) try {
				var clazz = Class.forName(annoData.memberName());
				if (TooltipRenderer.class.isAssignableFrom(clazz))
					OVERLAY_RENDERERS.add((TooltipRenderer) clazz.getDeclaredConstructor().newInstance());
			} catch (Exception e) {
				CCG.LOGGER.error("Unable to load tooltip renderer: {}", annoData.memberName(), e);
			}
		}));
	}
	public static void itemTooltip(@NotNull ItemTooltipEvent event) {
		if (!CCG.config.tooltip.extraItemTooltip) return;
		if (shouldSuppressInfo()) return;
		var stack = event.getItemStack();
		var tooltip = event.getToolTip();
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
		CCGLang.enabled(GogglesItem.isWearingGoggles(mc.player)).addTo(1, tooltip);
	}
	private static void backtank(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.backtank) return;
		if (!(stack.getItem() instanceof BacktankItem)) return;
		CCGLang.add(Component.translatable("create.gui.goggles.fluid_container.capacity").withStyle(ChatFormatting.GRAY))
			.fraction(BacktankUtil.getAir(stack), BacktankUtil.maxAir(stack))
			.addTo(1, tooltip);
	}
	private static void divingBoots(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.divingBoots) return;
		if (!(stack.getItem() instanceof DivingBootsItem)) return;
		CCGLang.enabled(CCG.config.misc.allowDivingBoot).addTo(1, tooltip);
	}
	private static void wrench(@NotNull ItemStack stack, List<Component> tooltip) {
		if (!CCG.config.tooltip.wrench) return;
		if (!(stack.getItem() instanceof WrenchItem)) return;
		CCGLang.add(Component.translatable("create_cyber_goggles.config.option.misc.wrench.leftClickFastDismantle"))
			.space()
			.enabled(CCG.config.misc.wrench.leftClickFastDismantle)
			.addTo(1, tooltip);
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
	public static void gatherComponents(@NotNull GatherComponents event) {
		var elements = event.getTooltipElements();
		// 第一遍：只读收集流体条目，取最大 preferred 条宽作为统一宽度（不消费 marker）
		var sharedBarWidth = 0;
		for (var element : elements) {
			if (!(element.left().orElse(null) instanceof Component comp)) continue;
			var fluid = TooltipComponentUtil.peekFluidEntry(comp);
			if (fluid == null) continue;
			var preferred = ClientFluidEntryTooltipComponent.preferredBarWidth(
				mc.font,
				fluid.fluid(),
				fluid.capacityMb(),
				fluid.label()
			);
			if (preferred > sharedBarWidth) sharedBarWidth = preferred;
		}
		// 第二遍：消费 marker 并原地替换（marker 与文本混行时剩余文本保留，UI 独立成行插入）
		for (var i = 0; i < elements.size(); ) {
			var left = elements.get(i).left().orElse(null);
			if (!(left instanceof Component comp)) {
				i++;
				continue;
			}
			var split = TooltipComponentUtil.consumeMarker(comp);
			if (split == null) {
				i++;
				continue;
			}
			var ui = split.data();
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
		var stack = event.getItemStack();
		var renderer = findRenderer(stack);
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
		var positioner = event.getTooltipPositioner();
		var screenWidth = event.getScreenWidth();
		var screenHeight = event.getScreenHeight();
		var pos = positioner.positionTooltip(screenWidth, screenHeight, event.getX(), event.getY(), tooltipWidth, tooltipHeight);
		// 上方 overlay 让位：overlay 顶部 = tooltip 顶部 - overlayHeight - 6，不顶出需 tooltip 顶部 >= overlayHeight + 6
		// 不足时二分下移 mouseY（renderTooltipInternal 用 preEvent.getX/Y 定位，当前帧生效，无需帧间状态）
		var minTop = overlayHeight + 6;
		if (pos.y() < minTop) {
			var low = event.getY();
			var high = screenHeight;
			while (low < high) {
				var mid = low + high >>> 1;
				var test = positioner.positionTooltip(screenWidth, screenHeight, event.getX(), mid, tooltipWidth, tooltipHeight);
				if (test.y() >= minTop) high = mid;
				else low = mid + 1;
			}
			if (low > event.getY()) {
				event.setY(low);
				pos = positioner.positionTooltip(screenWidth, screenHeight, event.getX(), low, tooltipWidth, tooltipHeight);
			}
		}
		// 完整 clamp 到屏幕内
		var overlayX = Mth.clamp(pos.x(), 0, Math.max(0, screenWidth - overlayWidth));
		var overlayY = Mth.clamp(pos.y() - overlayHeight - 6, 0, Math.max(0, screenHeight - overlayHeight));
		var gui = event.getGraphics();
		var pose = gui.pose();
		pose.pushPose();
		renderer.render(gui, stack, overlayX - 4, overlayY);
		// 登记已渲染区域，供后渲染的 GoggleOverlay 避让
		OverlayManager.upperBottom = Math.max(OverlayManager.upperBottom, overlayY + overlayHeight);
		pose.popPose();
	}
	public static @Nullable TooltipRenderer findRenderer(@NotNull ItemStack stack) {
		for (var renderer : OVERLAY_RENDERERS) if (renderer.supports(stack)) return renderer;
		return null;
	}
}
