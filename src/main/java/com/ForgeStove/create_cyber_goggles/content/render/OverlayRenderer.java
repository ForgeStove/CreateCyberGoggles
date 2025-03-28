package com.ForgeStove.create_cyber_goggles.content.render;
import com.ForgeStove.create_cyber_goggles.*;
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
public class OverlayRenderer {
	public static void register(RegisterGuiLayersEvent event) {
		event.registerAbove(
				VanillaGuiLayers.HOTBAR,
				ResourceLocation.fromNamespaceAndPath(CreateCyberGoggles.ID, "goggle_overlay"),
				OverlayRenderer::renderOverlay
		);
	}
	public static void renderOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null || player.hasContainerOpen() || mc.isPaused() || mc.screen != null) return;
		ItemStack itemStack;
		var level = mc.level;
		if (level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
		var blockEntity = level.getBlockEntity(blockHitResult.getBlockPos());
		if (Config.renderExtraItems.get() && blockEntity instanceof DepotBlockEntity depotBlockEntity)
			itemStack = depotBlockEntity.getHeldItem();
		else if (Config.renderExtraItems.get() && blockEntity instanceof PackagerBlockEntity packagerBlockEntity)
			itemStack = packagerBlockEntity.heldBox;
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
			var v = Math.max(15, speedLevel.getParticleSpeed()) * Math.signum(speed);
			level.addParticle(
					new RotationIndicatorParticleData(
							speedLevel.getColor(),
							v,
							kineticBlock.getParticleInitialRadius(),
							kineticBlock.getParticleTargetRadius(),
							10,
							rotationAxis
					), center.x, center.y, center.z, 0, 0, 0
			);
			return;
		} else return;
		renderItemStack(guiGraphics, itemStack);
	}
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
