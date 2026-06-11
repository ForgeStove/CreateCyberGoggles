package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public final class TextConfigEntry extends ConfigEntry {
	private final MultiLineTextWidget textWidget;
	private final List<MultiLineTextWidget> textWidgetAsList;
	public TextConfigEntry(ConfigCategoryTab<?> tab, Component text) {
		super();
		textWidget = new MultiLineTextWidget(text, tab.getMinecraft().font);
		textWidgetAsList = List.of(textWidget);
	}
	@NotNull
	@Override
	public List<? extends NarratableEntry> narratables() {
		return textWidgetAsList;
	}
	@Override
	public void extractContent(@NotNull GuiGraphicsExtractor gui, int mouseX, int mouseY, boolean hovered, float delta) {
		textWidget.setX(getX());
		textWidget.setY(getY() + 5);
		textWidget.setMaxWidth(getWidth());
		textWidget.extractWidgetRenderState(gui, mouseX, mouseY, delta);
	}
	@NotNull
	@Override
	public List<? extends GuiEventListener> children() {
		return textWidgetAsList;
	}
}
