package io.github.forgestove.create_cyber_goggles.config.client.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.client.ClientUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public final class TextConfigEntry extends ConfigEntry {
	private final MultiLineTextWidget textWidget;
	private final List<MultiLineTextWidget> textWidgetAsList;
	public TextConfigEntry(Component text) {
		super();
		textWidget = new MultiLineTextWidget(text, ClientUtil.mc.font);
		textWidgetAsList = List.of(textWidget);
	}
	@NotNull
	@Override
	public List<? extends NarratableEntry> narratables() {
		return textWidgetAsList;
	}
	@Override
	public void render(
		@NotNull GuiGraphics gui,
		int index,
		int y,
		int x,
		int width,
		int height,
		int mouseX,
		int mouseY,
		boolean hovered,
		float delta
	) {
		textWidget.setX(x);
		textWidget.setY(y + 5);
		textWidget.setMaxWidth(width);
		textWidget.renderWidget(gui, mouseX, mouseY, delta);
	}
	@NotNull
	@Override
	public List<? extends GuiEventListener> children() {
		return textWidgetAsList;
	}
}
