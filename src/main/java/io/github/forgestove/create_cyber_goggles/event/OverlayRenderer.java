package io.github.forgestove.create_cyber_goggles.event;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import io.github.forgestove.create_cyber_goggles.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
public class OverlayRenderer {
	public static void renderOverlay(GuiGraphics guiGraphics, float ignoredTickDelta) {
		if (!CCG.CONFIG.goggles.renderExtraItems) return;
		var mc = Minecraft.getInstance();
		if (mc.isPaused() || mc.screen != null || mc.options.hideGui) return;
		var be = Common.getSelectedBE();
		if (be instanceof DepotBlockEntity dbe) Common.renderItemStack(guiGraphics, dbe.getHeldItem());
	}
}
