package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.forgestove.create_cyber_goggles.content.util.Common;
import com.simibubi.create.content.kinetics.base.IRotate.SpeedLevel;
import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.utility.VecHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.BlockHitResult;
public class OverlayRenderer {
	public static void register() {
		HudRenderCallback.EVENT.register(OverlayRenderer::renderOverlay);
	}
	public static void renderOverlay(GuiGraphics guiGraphics, float tickDelta) {
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null || mc.isPaused() || mc.screen != null) return;
		var level = mc.level;
		if (level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
		var blockEntity = level.getBlockEntity(blockHitResult.getBlockPos());
		var renderExtraItems = CCGConfig.getConfig().goggles.renderExtraItems;
		if (renderExtraItems && blockEntity instanceof DepotBlockEntity depotBlockEntity)
			Common.renderItemStack(guiGraphics, depotBlockEntity.getHeldItem());
		else if (CCGConfig.getConfig().goggles.enableKineticEffect && blockEntity instanceof KineticBlockEntity kineticBlockEntity) {
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
					rotationAxis.name().charAt(0)
				), center.x, center.y, center.z, 0, 0, 0
			);
		}
	}
}
