package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.forgestove.create_cyber_goggles.content.util.Common;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
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
		if (!CCGConfig.get().goggles.renderExtraItems) return;
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null || mc.isPaused() || mc.screen != null) return;
		var level = mc.level;
		if (level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
		var blockEntity = level.getBlockEntity(blockHitResult.getBlockPos());
		if (blockEntity != null) switch (blockEntity) {
			case DepotBlockEntity depotBlockEntity -> Common.renderItemStack(guiGraphics, depotBlockEntity.getHeldItem());
			case PackagerBlockEntity packagerBlockEntity -> Common.renderItemStack(guiGraphics, packagerBlockEntity.heldBox);
			default -> {}
		}
	}
}
