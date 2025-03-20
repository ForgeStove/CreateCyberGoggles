package com.ForgeStove.create_cyber_goggles.render;
import com.ForgeStove.create_cyber_goggles.*;
import com.simibubi.create.content.kinetics.base.IRotate.SpeedLevel;
import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.LayeredDraw.Layer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.*;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class OverlayRenderer {
	public static final Layer OVERLAY = OverlayRenderer::renderOverlay;
	public static void register(RegisterGuiLayersEvent event) {
		event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(CreateCyberGoggles.ID, "overlay"), OVERLAY);
	}
	public static void renderOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || player.hasContainerOpen() || mc.isPaused()) return;
		ItemStack itemStack;
		ClientLevel level = mc.level;
		if (level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
		if (blockHitResult.getType() == Type.MISS) return;
		BlockEntity blockEntity = level.getBlockEntity(blockHitResult.getBlockPos());
		if (Config.enableDepotRender.get() && blockEntity instanceof DepotBlockEntity depotBlockEntity)
			itemStack = depotBlockEntity.getHeldItem();
		else if (Config.enableKineticEffect.get() && blockEntity instanceof KineticBlockEntity kineticBlockEntity) {
			if (blockHitResult.getType() == Type.MISS) return;
			if (!blockHitResult.getBlockPos().equals(kineticBlockEntity.getBlockPos())) return;
			float speed = kineticBlockEntity.getSpeed();
			if (speed == 0) return;
			BlockState state = kineticBlockEntity.getBlockState();
			if (!(state.getBlock() instanceof KineticBlock kineticBlock)) return;
			Axis rotationAxis = kineticBlock.getRotationAxis(state);
			if (rotationAxis == null) return;
			Vec3 center = VecHelper.getCenterOf(kineticBlockEntity.getBlockPos());
			SpeedLevel speedLevel = SpeedLevel.of(speed);
			int particleSpeed = Math.max(15, speedLevel.getParticleSpeed());
			float v = particleSpeed * Math.signum(speed);
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
	public static void renderItemStack(GuiGraphics guiGraphics, @NotNull ItemStack itemStack) {
		if (itemStack.isEmpty()) return;
		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;
		Default tooltipFlag = mc.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
		List<Component> tooltipLines = itemStack.getTooltipLines(TooltipContext.of(mc.level), mc.player, tooltipFlag);
		int tooltipHeight = tooltipLines.size() * font.lineHeight + 8;
		int x = guiGraphics.guiWidth() / 2;
		int y = guiGraphics.guiHeight() / 2;
		guiGraphics.renderItem(itemStack, x + 10, y - 15);
		guiGraphics.renderItemDecorations(font, itemStack, x + 10, y - 15);
		guiGraphics.renderComponentTooltip(font, tooltipLines, x + 22, y - Math.max(10, tooltipHeight - 75));
	}
}
