package io.github.forgestove.create_cyber_goggles.event;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import com.simibubi.create.content.logistics.crate.CreativeCrateBlockEntity;
import com.simibubi.create.content.logistics.depot.*;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.State;
import com.simibubi.create.infrastructure.config.AllConfigs;
import io.github.forgestove.create_cyber_goggles.*;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.CreativeCrateBlockEntityAccessor;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.*;
import org.jetbrains.annotations.*;
public class OverlayRenderer {
	public static int hoverTicks;
	public static float fade;
	public static ItemStack currentItemStack;
	public static ItemStack cannonItemStack;
	public static void registerLayer(@NotNull RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "goggle_overlay", OverlayRenderer::renderOverlay);
	}
	public static void renderOverlay(ForgeGui gui, GuiGraphics guiGraphics, float partialTicks, int width, int height) {
		if (!CCG.CONFIG.goggles.renderExtraItems) return;
		var mc = Minecraft.getInstance();
		if (mc.isPaused() || mc.screen != null || mc.options.hideGui) {
			currentItemStack = null;
			hoverTicks = 0;
			return;
		}
		fade = Mth.clamp((hoverTicks++ + partialTicks) / 24f, 0, 1);
		var itemStack = toRenderItemStack();
		currentItemStack = itemStack;
		if (itemStack == null) {
			hoverTicks = 0;
			return;
		}
		renderItemStack(guiGraphics, itemStack);
	}
	public static void tickColor(@NotNull RenderTooltipEvent.Color event) {
		if (!event.getItemStack().equals(currentItemStack)) return;
		var cfg = AllConfigs.client();
		var colorBackground = cfg.overlayCustomColor.get()
			? new Color(cfg.overlayBackgroundColor.get())
			: BoxElement.COLOR_VANILLA_BACKGROUND.scaleAlpha(.75f);
		if (fade < 1) colorBackground.scaleAlpha(fade);
		event.setBackground(colorBackground.getRGB());
	}
	public static @Nullable ItemStack toRenderItemStack() {
		var be = CCGHelper.getBE();
		if (be instanceof DepotBlockEntity dbe) return dbe.getHeldItem();
		else if (be instanceof EjectorBlockEntity ebe) return ebe.getBehaviour(DepotBehaviour.TYPE).getHeldItemStack();
		else if (be instanceof PackagerBlockEntity pbe) return pbe.heldBox;
		else if (be instanceof ChuteBlockEntity cbe) return cbe.getItem();
		else if (be instanceof MechanicalCrafterBlockEntity mcbe) return mcbe.getInventory().getItem(0);
		else if (be instanceof CrushingWheelControllerBlockEntity cwcb) return cwcb.inventory.getStackInSlot(0);
		else if (be instanceof MillstoneBlockEntity mbe) return mbe.inputInv.getStackInSlot(0);
		else if (be instanceof CreativeCrateBlockEntity ccbe) return ((CreativeCrateBlockEntityAccessor) ccbe).getFiltering().getFilter();
		else if (be instanceof SchematicannonBlockEntity sbe) {
			if (sbe.state.equals(State.STOPPED)) cannonItemStack = null;
			return cannonItemStack == null ? sbe.missingItem : cannonItemStack;
		} else if (be instanceof BeltBlockEntity bbe) {
			var inventory = bbe.getInventory();
			if (inventory == null) return null;
			var stackAtOffset = inventory.getStackAtOffset(bbe.index);
			return stackAtOffset == null ? null : stackAtOffset.stack;
		}
		var e = CCGHelper.getE();
		if (e instanceof PackageEntity pe) return pe.getBox();
		return null;
	}
	/**
	 * 在屏幕中央区域渲染指定物品堆的图标及关联的悬浮提示信息。
	 *
	 * @param guiGraphics GUI渲染上下文对象，用于执行图形绘制操作
	 * @param itemStack   需要渲染的物品堆实例。若值为null或空物品堆叠时方法立即返回
	 */
	public static void renderItemStack(GuiGraphics guiGraphics, ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty()) return;
		var mc = Minecraft.getInstance();
		var font = mc.font;
		var tooltipFlag = mc.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
		var tooltipLines = itemStack.getTooltipLines(mc.player, tooltipFlag);
		var height = tooltipLines.size() * font.lineHeight;
		var cfg = AllConfigs.client();
		var offsetX = fade < 1 ? (int) (
			Math.pow(1 - fade, 3) * Math.signum(cfg.overlayOffsetX.get() + .5f) * 8
		) : 0;
		var x = guiGraphics.guiWidth() / 2 + cfg.overlayOffsetX.get() + offsetX;
		var y = guiGraphics.guiHeight() / 2 + cfg.overlayOffsetY.get();
		if (GoggleOverlayRenderer.hoverTicks != 0) y -= height + 20;
		guiGraphics.renderItem(itemStack, x - 10, y - 10);
		guiGraphics.renderItemDecorations(font, itemStack, x - 10, y - 10);
		guiGraphics.renderTooltip(font, itemStack, x, y);
	}
}
