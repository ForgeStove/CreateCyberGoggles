package com.forgestove.create_cyber_goggles.event;
import com.forgestove.create_cyber_goggles.CCG;
import com.forgestove.create_cyber_goggles.util.Common;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
public class OverlayRenderer {
	public static void register() {
		HudRenderCallback.EVENT.register(OverlayRenderer::renderOverlay);
	}
	public static void renderOverlay(GuiGraphics guiGraphics, float tickDelta) {
		if (!CCG.CONFIG.goggles.renderExtraItems) return;
		var mc = Minecraft.getInstance();
		if (mc.isPaused() || mc.screen != null) return;
		var be = Common.getSelectedBE();
		if (be instanceof DepotBlockEntity dbe) Common.renderItemStack(guiGraphics, dbe.getHeldItem());
	}
}
