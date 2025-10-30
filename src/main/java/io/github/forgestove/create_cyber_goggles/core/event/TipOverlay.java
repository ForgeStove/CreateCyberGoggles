package io.github.forgestove.create_cyber_goggles.core.event;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.*;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class TipOverlay {
	public static List<MutableComponent> lastTip;
	public static int hoverTicks;
	public static int deltaX, deltaY;
	public static void register(@NotNull RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "tip_overlay", TipOverlay::renderOverlay);
	}
	public static void renderOverlay(ForgeGui gui, GuiGraphics graphics, float partialTicks, int width, int height) {
		if (mc.options.hideGui) return;
		if (hoverTicks == 0 || lastTip == null) return;
		var x = graphics.guiWidth() / 2 + deltaX;
		var y = graphics.guiHeight() - 75 - lastTip.size() * 12 + deltaY;
		var alpha = hoverTicks > 5 ? (11 - hoverTicks) / 5F : Math.min(1, hoverTicks / 5F);
		var color = new Color(0xFFFFFF).setAlpha(alpha);
		var titleColor = new Color(0xFBDC7D).setAlpha(alpha);
		var i = 0;
		for (var component : lastTip) {
			graphics.drawString(mc.font, component, x - mc.font.width(component) / 2, y + i * 12, (i == 0 ? titleColor : color).getRGB());
			i++;
		}
	}
	/**
	 * 每次滴答发生时，此方法应在{@link TipOverlay#tick(ClientTickEvent)}之前运行
	 * <p>
	 * 以确保{@link TipOverlay#hoverTicks}的状态正确更新。
	 */
	public static void show(List<MutableComponent> tip, int x, int y) {
		if (mc.screen != null || hasActivedValueBox()) return;
		hoverTicks = hoverTicks == 0 ? 11 : Math.max(hoverTicks, 6);
		lastTip = tip;
		deltaX = x;
		deltaY = y;
	}
	public static void show(List<MutableComponent> tip) {
		show(tip, 0, 0);
	}
	public static void tick(ClientTickEvent ignoredEvent) {
		if (hoverTicks > 0) hoverTicks--;
	}
}
