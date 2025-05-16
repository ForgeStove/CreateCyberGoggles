package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.forgestove.create_cyber_goggles.content.util.Common;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
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
		if (!CCGConfig.get().goggles.renderExtraItems) return;
		var mc = Minecraft.getInstance();
		if (mc.player == null || mc.isPaused() || mc.screen != null) return;
		var level = mc.level;
		if (level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
		var blockEntity = level.getBlockEntity(blockHitResult.getBlockPos());
		if (blockEntity instanceof DepotBlockEntity depotBlockEntity) Common.renderItemStack(guiGraphics, depotBlockEntity.getHeldItem());
		if (blockEntity instanceof PackagerBlockEntity packagerBlockEntity)
			Common.renderItemStack(guiGraphics, packagerBlockEntity.heldBox);
	}
}
