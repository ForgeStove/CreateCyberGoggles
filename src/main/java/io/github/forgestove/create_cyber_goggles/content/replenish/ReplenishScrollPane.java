package io.github.forgestove.create_cyber_goggles.content.replenish;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiScrollPane;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.BiConsumer;

import static io.github.forgestove.create_cyber_goggles.content.replenish.ReplenishLayout.SCROLL_W;
class ReplenishScrollPane extends UiScrollPane {
	private static final int BAR_GAP = 4;
	private BiConsumer<GuiGraphics, Double> content = (gui, scroll) -> {};
	void content(BiConsumer<GuiGraphics, Double> content) {
		this.content = content;
	}
	@Override
	protected void renderContent(UiContext ui) {
		content.accept(ui.graphics, scroll());
	}
	@Override
	protected UiBounds barBounds() {
		var b = bounds();
		return new UiBounds(b.right() - BAR_GAP - SCROLL_W, b.y(), SCROLL_W, b.height());
	}
	@Override
	protected void paintShadows(UiContext ui) {}
	@Override
	protected void paintBar(UiContext ui) {
		if (!shown()) return;
		var bar = barBounds();
		int knob = knobHeight();
		int knobY = knobY(knob);
		var gui = ui.graphics;
		var pad = AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_PAD;
		gui.blit(pad.location, bar.x(), knobY, SCROLL_W, knob, pad.getStartX(), pad.getStartY(), pad.getWidth(), pad.getHeight(), 256,
			256);
		AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_TOP.render(gui, bar.x(), knobY);
		if (knob > 16) AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_MID.render(gui, bar.x(), knobY + knob / 2 - 4);
		AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_BOT.render(gui, bar.x(), knobY + knob - 5);
	}
}
