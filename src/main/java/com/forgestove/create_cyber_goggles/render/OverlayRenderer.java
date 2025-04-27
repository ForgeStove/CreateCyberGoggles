package com.forgestove.create_cyber_goggles.render;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import com.forgestove.create_cyber_goggles.util.Util;
import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.*;
import org.jetbrains.annotations.NotNull;
public class OverlayRenderer {
	public static void register(@NotNull RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "goggle_overlay", OverlayRenderer::renderOverlay);
	}
	public static void renderOverlay(ForgeGui forgeGui, GuiGraphics guiGraphics, float v, int i, int i1) {
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null || mc.isPaused() || mc.screen != null) return;
		var level = mc.level;
		if (level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
		var blockEntity = level.getBlockEntity(blockHitResult.getBlockPos());
		var renderExtraItems = CreateCyberGoggles.config.goggles.renderExtraItems;
		if (renderExtraItems && blockEntity instanceof DepotBlockEntity depotBlockEntity)
			Util.renderItemStack(guiGraphics, depotBlockEntity.getHeldItem());
		else if (renderExtraItems && blockEntity instanceof PackagerBlockEntity packagerBlockEntity)
			Util.renderItemStack(guiGraphics, packagerBlockEntity.heldBox);
		else if (CreateCyberGoggles.config.goggles.enableKineticEffect && blockEntity instanceof KineticBlockEntity kineticBlockEntity) {
			if (!blockHitResult.getBlockPos().equals(kineticBlockEntity.getBlockPos())) return;
			var speed = kineticBlockEntity.getSpeed();
			if (speed == 0) return;
			var state = kineticBlockEntity.getBlockState();
			if (!(state.getBlock() instanceof KineticBlock kineticBlock)) return;
			var rotationAxis = kineticBlock.getRotationAxis(state);
			if (rotationAxis == null) return;
			var center = VecHelper.getCenterOf(kineticBlockEntity.getBlockPos());
			var speedLevel = IRotate.SpeedLevel.of(speed);
			level.addParticle(
					new RotationIndicatorParticleData(
							speedLevel.getColor(),
							Math.max(15, speedLevel.getParticleSpeed()) * Math.signum(speed),
							kineticBlock.getParticleInitialRadius(),
							kineticBlock.getParticleTargetRadius(),
							10,
							rotationAxis.name().charAt(0)
					), center.x, center.y, center.z, 0, 0, 0
			);
		}
	}
}
