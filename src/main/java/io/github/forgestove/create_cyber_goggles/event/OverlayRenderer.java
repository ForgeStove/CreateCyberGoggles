package io.github.forgestove.create_cyber_goggles.event;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import io.github.forgestove.create_cyber_goggles.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.*;
import org.jetbrains.annotations.NotNull;
public class OverlayRenderer {
	public static void register(@NotNull RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "goggle_overlay", OverlayRenderer::renderOverlay);
	}
	public static void renderOverlay(ForgeGui forgeGui, GuiGraphics guiGraphics, float v, int i, int i1) {
		if (!CCG.CONFIG.goggles.renderExtraItems) return;
		var mc = Minecraft.getInstance();
		if (mc.isPaused() || mc.screen != null || mc.options.hideGui) return;
		var be = Common.getBE();
		if (be instanceof DepotBlockEntity dbe) Common.renderItemStack(guiGraphics, dbe.getHeldItem());
	}
}
