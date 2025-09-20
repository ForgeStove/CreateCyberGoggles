package io.github.forgestove.create_cyber_goggles.event;
import com.zurrtum.create.content.logistics.depot.DepotBlockEntity;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import io.github.forgestove.create_cyber_goggles.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
public class OverlayRenderer {
	public static void renderOverlay(GuiGraphics guiGraphics, DeltaTracker ignoredDeltaTracker) {
		if (!CCG.CONFIG.goggles.renderExtraItems) return;
		var mc = Minecraft.getInstance();
		if (mc.isPaused() || mc.screen != null || mc.options.hideGui) return;
		var be = Common.getBE();
		if (be instanceof DepotBlockEntity dbe) {
			var heldItem = dbe.getHeldItem();
			if (heldItem != null) Common.renderItemStack(guiGraphics, heldItem.stack);
		}
		if (be instanceof PackagerBlockEntity pbe) Common.renderItemStack(guiGraphics, pbe.heldBox);
	}
}
