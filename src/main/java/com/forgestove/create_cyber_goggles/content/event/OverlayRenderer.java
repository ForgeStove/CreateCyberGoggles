package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.forgestove.create_cyber_goggles.content.util.Common;
import com.simibubi.create.content.kinetics.base.IRotate.SpeedLevel;
import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.*;
public class OverlayRenderer {
	public static void register(RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "goggle_overlay", OverlayRenderer::renderOverlay);
	}
	public static void renderOverlay(ForgeGui forgeGui, GuiGraphics guiGraphics, float v, int i, int i1) {
		var mc = Minecraft.getInstance();
		if (mc.player == null || mc.isPaused() || mc.screen != null) return;
		var level = mc.level;
		if (level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
		var blockEntity = level.getBlockEntity(blockHitResult.getBlockPos());
		var goggles = CCGConfig.get().goggles;
		if (goggles.renderExtraItems && blockEntity instanceof DepotBlockEntity depotBlockEntity)
			Common.renderItemStack(guiGraphics, depotBlockEntity.getHeldItem());
		else if (goggles.renderExtraItems && blockEntity instanceof PackagerBlockEntity packagerBlockEntity)
			Common.renderItemStack(guiGraphics, packagerBlockEntity.heldBox);
		else if (goggles.enableKineticEffect && blockEntity instanceof KineticBlockEntity kineticBlockEntity) {
			var speed = kineticBlockEntity.getSpeed();
			if (speed == 0) return;
			var state = kineticBlockEntity.getBlockState();
			if (!(state.getBlock() instanceof KineticBlock kineticBlock)) return;
			var rotationAxis = kineticBlock.getRotationAxis(state);
			if (rotationAxis == null) return;
			var center = VecHelper.getCenterOf(kineticBlockEntity.getBlockPos());
			var speedLevel = SpeedLevel.of(speed);
			var particleData = new RotationIndicatorParticleData(
				speedLevel.getColor(),
				Math.max(15, speedLevel.getParticleSpeed()) * Math.signum(speed),
				kineticBlock.getParticleInitialRadius(),
				kineticBlock.getParticleTargetRadius(),
				10,
				rotationAxis.name().charAt(0)
			);
			level.addParticle(particleData, center.x, center.y, center.z, 0, 0, 0);
		}
	}
}
