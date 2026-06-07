package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.Translation;
import io.github.forgestove.create_cyber_goggles.config.gui.*;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.*;

import java.util.*;
public abstract class ValueConfigEntry<C, T, V> extends ConfigEntry {
	public final Button resetButton;
	public final Button undoButton;
	public final ConfigCategoryTab<C> tab;
	public final List<AbstractWidget> children = new ArrayList<>();
	public final ValueConfigNode<C, T, V> valueNode;
	public final Component label;
	@Nullable public final List<FormattedCharSequence> tooltip;
	public List<FormattedCharSequence> tooltipWithError;
	@Nullable public Component validationError;
	public boolean hasChanged;
	public ValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, T, V> valueNode) {
		this.tab = tab;
		label = valueNode.getTitle().copy().withStyle(ChatFormatting.WHITE);
		var font = tab.getMinecraft().font;
		tooltip = valueNode.getTooltip() == null ? null : font.split(valueNode.getTooltip(), 350);
		tooltipWithError = getTooltipWithError();
		this.valueNode = valueNode;
		resetButton = Button.builder(Translation.RESET_LABEL, b -> resetToDefault())
			.size(Math.max(font.width(Translation.RESET_LABEL) + 6, SIZE), SIZE)
			.build();
		resetButton.active = !valueNode.isDefaultValue(this.tab.getConfig());
		children.add(resetButton);
		undoButton = Button.builder(Translation.UNDO_LABEL, b -> resetToActive())
			.size(Math.max(font.width(Translation.UNDO_LABEL) + 6, SIZE), SIZE)
			.build();
		undoButton.active = !valueNode.isActiveValue(this.tab.getConfig());
		children.add(undoButton);
	}
	public void resetToDefault() {
		valueNode.resetToDefault();
		tab.getScreen().refresh();
	}
	public void resetToActive() {
		valueNode.resetToActive(tab.getConfig());
		tab.getScreen().refresh();
	}
	public V getValue() {
		return valueNode.getEditingValue(tab.getConfig());
	}
	public void setValue(V value) {
		valueNode.setEditingValue(value);
		tab.getScreen().refresh();
	}
	public void refresh() {
		resetButton.active = !valueNode.isDefaultValue(tab.getConfig());
		undoButton.active = !valueNode.isActiveValue(tab.getConfig());
		validationError = valueNode.validate(tab.getConfig());
		hasChanged = !valueNode.isActiveValue(tab.getConfig());
		tooltipWithError = getTooltipWithError();
	}
	@Override
	public boolean hasError() {
		return validationError != null;
	}
	@NotNull
	@Override
	public List<? extends GuiEventListener> children() {
		return children;
	}
	@NotNull
	@Override
	public List<? extends NarratableEntry> narratables() {
		return children;
	}
	@Nullable
	public List<FormattedCharSequence> getTooltip() {
		return hasError() ? tooltipWithError : tooltip;
	}
	public List<FormattedCharSequence> getTooltipWithError() {
		if (!hasError()) return tooltip;
		List<FormattedCharSequence> errorTooltip = new ArrayList<>();
		if (tooltip != null) errorTooltip.addAll(tooltip);
		if (validationError != null) errorTooltip.add(validationError.copy().withStyle(ChatFormatting.RED).getVisualOrderText());
		return errorTooltip;
	}
	public void renderLabel(GuiGraphics gui, int x, int y) {
		var l = GuiUtil.styleAsState(label, hasError(), hasChanged);
		gui.drawString(tab.getMinecraft().font, l.getVisualOrderText(), x, y + 5, -1, false);
	}
	public void layoutRightToLeft(int x, int y, int entryWidth, AbstractWidget... widgets) {
		var right = x + entryWidth;
		for (var widget : widgets) {
			right -= widget.getWidth();
			widget.setX(right);
			widget.setY(y);
			right -= GAP;
		}
	}
	public void renderWidgets(GuiGraphics gui, int mouseX, int mouseY, float delta, Renderable... widgets) {
		for (var widget : widgets) widget.render(gui, mouseX, mouseY, delta);
	}
	public void renderGui(
		@NotNull GuiGraphics gui,
		int y,
		int x,
		int width,
		int mouseX,
		int mouseY,
		float partialTick,
		AbstractWidget... widgets
	) {
		renderLabel(gui, x, y);
		layoutRightToLeft(x, y, width, widgets);
		renderWidgets(gui, mouseX, mouseY, partialTick, widgets);
	}
}
