package com.forgestove.create_cyber_goggles.event;
import com.forgestove.create_cyber_goggles.CCG;
import com.forgestove.create_cyber_goggles.util.Common;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.NotNull;
public class OverlayRenderer {
	public static void register(@NotNull RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.HOTBAR,
			ResourceLocation.fromNamespaceAndPath(CCG.ID, "goggle_overlay"),
			OverlayRenderer::renderOverlay);
	}
	public static void renderOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if (!CCG.CONFIG.goggles.renderExtraItems) return;
		var mc = Minecraft.getInstance();
		if (mc.isPaused() || mc.screen != null) return;
		var be = Common.getSelectedBE();
		if (be instanceof DepotBlockEntity dbe) Common.renderItemStack(guiGraphics, dbe.getHeldItem());
		if (be instanceof PackagerBlockEntity pbe) Common.renderItemStack(guiGraphics, pbe.heldBox);
	}
}
