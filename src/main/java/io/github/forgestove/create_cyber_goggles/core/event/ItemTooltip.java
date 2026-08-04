package io.github.forgestove.create_cyber_goggles.core.event;
import com.mojang.datafixers.util.Either;
import com.simibubi.create.content.equipment.armor.*;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.*;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RenderTooltipEvent.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.fluids.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2ic;

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
	public static void renderTooltipPre(@NotNull Pre event) {
		if (!CCG.config.tooltip.extraItemTooltip) return;
		var stack = event.getItemStack();
		TooltipRenderer renderer = null;
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
		var rawOverlayY = getOverlayY(pos, overlayHeight) + OverlayManager.overallOffsetY;
		// 触底：期望位置底部越界 → 更新整体缩放（供 Goggle/TooltipOverlay 下一帧使用）
		if (rawOverlayY + overlayHeight > event.getScreenHeight())
			OverlayManager.overallScale = Math.min(
				OverlayManager.overallScale,
				(float) event.getScreenHeight() / (rawOverlayY + overlayHeight)
			);
		var overlayY = rawOverlayY;
		// 触顶：更新整体下移量（供 Goggle/TooltipOverlay 下一帧使用），自身钳到顶部
		if (overlayY < 0) {
			OverlayManager.overallOffsetY = Math.max(OverlayManager.overallOffsetY, -overlayY);
			overlayY = 0;
		}
		// 完整 clamp 到屏幕内（避免越界）
		overlayX = Mth.clamp(overlayX, 0, Math.max(0, event.getScreenWidth() - overlayWidth));
		overlayY = Mth.clamp(overlayY, 0, Math.max(0, event.getScreenHeight() - overlayHeight));
		// 缩放兜底（含整体缩放）：仍越界则缩放（围绕 overlay 中心缩放）
		var scale = OverlayManager.overallScale;
		if (overlayX + overlayWidth > event.getScreenWidth())
			scale = Math.min(scale, (float) (event.getScreenWidth() - overlayX) / overlayWidth);
		if (overlayY + overlayHeight > event.getScreenHeight())
			scale = Math.min(scale, (float) (event.getScreenHeight() - overlayY) / overlayHeight);
		scale = Mth.clamp(scale, 0.1F, 1F);
		// 更新整体缩放锚点：已占用区域 + 自身包围盒中心
		var minY = overlayY;
		var maxY = overlayY + overlayHeight;
		for (var rect : OverlayManager.getOccupied()) {
			minY = Math.min(minY, rect.getY());
			maxY = Math.max(maxY, rect.getY() + rect.getHeight());
		}
		// 水平围绕屏幕中心（避免偏右），垂直围绕整体包围盒中心
		OverlayManager.scaleCenterX = event.getScreenWidth() / 2;
		OverlayManager.scaleCenterY = (minY + maxY) / 2;
		var graphics = event.getGraphics();
		var pose = graphics.pose();
		pose.pushPose();
		if (scale < 1) {
			pose.translate(OverlayManager.scaleCenterX, OverlayManager.scaleCenterY, 0);
			pose.scale(scale, scale, 1);
			pose.translate(-OverlayManager.scaleCenterX, -OverlayManager.scaleCenterY, 0);
		}
		renderer.render(graphics, stack, overlayX - 4, overlayY);
		OverlayManager.upperBottom = Math.max(OverlayManager.upperBottom, overlayY + overlayHeight);
		pose.popPose();
	}
	private static @NotNull Vector2ic getPos(@NotNull Pre event, int width, int height) {
		return event.getTooltipPositioner()
			.positionTooltip(event.getScreenWidth(), event.getScreenHeight(), event.getX(), event.getY(), width, height);
	}
	private static int getOverlayX(@NotNull Pre event, Vector2ic pos, int overlayWidth) {
		return Mth.clamp(pos.x(), 0, Math.max(0, event.getScreenWidth() - overlayWidth));
	}
	private static int getOverlayY(Vector2ic pos, int overlayHeight) {
		return pos.y() - overlayHeight - 6;
	}
}
