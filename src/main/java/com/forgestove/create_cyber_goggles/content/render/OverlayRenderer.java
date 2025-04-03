package com.forgestove.create_cyber_goggles.content.render;
import com.forgestove.create_cyber_goggles.*;
import com.simibubi.create.content.kinetics.base.IRotate.SpeedLevel;
import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.NotNull;
public class OverlayRenderer {
	public static void register(@NotNull RegisterGuiLayersEvent event) {
		event.registerAbove(
				VanillaGuiLayers.HOTBAR,
				ResourceLocation.fromNamespaceAndPath(CreateCyberGoggles.ID, "goggle_overlay"),
				OverlayRenderer::renderOverlay
		);
	}
	public static void renderOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null || mc.isPaused() || mc.screen != null) return;
		var level = mc.level;
		if (level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
		var blockEntity = level.getBlockEntity(blockHitResult.getBlockPos());
		boolean renderExtraItems = Config.renderExtraItems.get();
		if (renderExtraItems && blockEntity instanceof DepotBlockEntity depotBlockEntity)
			renderItemStack(guiGraphics, depotBlockEntity.getHeldItem());
		else if (renderExtraItems && blockEntity instanceof PackagerBlockEntity packagerBlockEntity)
			renderItemStack(guiGraphics, packagerBlockEntity.heldBox);
		else if (Config.enableKineticEffect.get() && blockEntity instanceof KineticBlockEntity kineticBlockEntity) {
			if (!blockHitResult.getBlockPos().equals(kineticBlockEntity.getBlockPos())) return;
			var speed = kineticBlockEntity.getSpeed();
			if (speed == 0) return;
			var state = kineticBlockEntity.getBlockState();
			if (!(state.getBlock() instanceof KineticBlock kineticBlock)) return;
			var rotationAxis = kineticBlock.getRotationAxis(state);
			if (rotationAxis == null) return;
			var center = VecHelper.getCenterOf(kineticBlockEntity.getBlockPos());
			var speedLevel = SpeedLevel.of(speed);
			level.addParticle(
					new RotationIndicatorParticleData(
							speedLevel.getColor(),
							Math.max(15, speedLevel.getParticleSpeed()) * Math.signum(speed),
							kineticBlock.getParticleInitialRadius(),
							kineticBlock.getParticleTargetRadius(),
							10,
							rotationAxis
					), center.x, center.y, center.z, 0, 0, 0
			);
		}
	}
	/**
	 * 在屏幕中央区域渲染指定物品堆的图标及关联的悬浮提示信息。
	 *
	 * @param guiGraphics GUI渲染上下文对象，用于执行图形绘制操作
	 * @param itemStack   需要渲染的物品堆实例。若值为null或空物品堆叠时方法立即返回
	 * @implNote 渲染位置逻辑：
	 * 		<p>物品图标绘制在屏幕水平中央偏右10像素、垂直中央偏上15像素的位置
	 * 		<p>物品装饰层（如数量文本）叠加在图标相同位置
	 * 		<p>悬浮提示框根据提示行数自适应高度，水平位置位于图标右侧12像素
	 * 		<p>垂直位置根据提示行数动态计算以避免溢出屏幕
	 */
	public static void renderItemStack(GuiGraphics guiGraphics, ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty()) return;
		var mc = Minecraft.getInstance();
		var font = mc.font;
		var tooltipFlag = mc.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
		var tooltipLines = itemStack.getTooltipLines(TooltipContext.of(mc.level), mc.player, tooltipFlag);
		var height = Math.max(10, tooltipLines.size() * font.lineHeight - 60);
		var x = guiGraphics.guiWidth() / 2;
		var y = guiGraphics.guiHeight() / 2;
		guiGraphics.renderItem(itemStack, x + 10, y - 15);
		guiGraphics.renderItemDecorations(font, itemStack, x + 10, y - 15);
		guiGraphics.renderTooltip(font, itemStack, x + 22, y - height);
	}
}
