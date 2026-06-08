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
public abstract class ValueConfigEntry<C, V> extends ConfigEntry {
	public final Button resetButton;
	public final Button undoButton;
	public final ConfigCategoryTab<C> tab;
	public final List<AbstractWidget> children = new ArrayList<>();
	public final ValueConfigNode<C, V> valueNode;
	public final Component label;
	@Nullable public final List<FormattedCharSequence> tooltip;
	public List<FormattedCharSequence> tooltipWithError;
	@Nullable public Component validationError;
	public boolean hasChanged;
	public ValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, V> node) {
		this.tab = tab;
		label = node.getTitle().copy().withStyle(ChatFormatting.WHITE);
		var font = tab.getMinecraft().font;
		tooltip = node.getTooltip() == null ? null : font.split(node.getTooltip(), 350);
		tooltipWithError = getTooltipWithError();
		valueNode = node;
		resetButton = Button.builder(Translation.RESET_LABEL, b -> resetToDefault())
			.size(Math.max(font.width(Translation.RESET_LABEL) + 6, SIZE), SIZE)
			.build();
		undoButton = Button.builder(Translation.UNDO_LABEL, b -> resetToActive())
			.size(Math.max(font.width(Translation.UNDO_LABEL) + 6, SIZE), SIZE)
			.build();
		resetButton.active = !node.isDefaultValue(this.tab.getConfig());
		undoButton.active = !node.isActiveValue(this.tab.getConfig());
		children.add(resetButton);
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
		var errorTooltip = new ArrayList<FormattedCharSequence>();
		if (tooltip != null) errorTooltip.addAll(tooltip);
		if (validationError != null) errorTooltip.add(validationError.copy().withStyle(ChatFormatting.RED).getVisualOrderText());
		return errorTooltip;
	}
	public void renderGui(
		@NotNull GuiGraphics gui,
		int y,
		int x,
		int width,
		int mouseX,
		int mouseY,
		float delta,
		AbstractWidget... widgets
	) {
		var label = GuiUtil.styleAsState(this.label, hasError(), hasChanged);
		gui.drawString(tab.getMinecraft().font, label.getVisualOrderText(), x, y + 5, -1, false);
		var right = x + width;
		for (var widget : widgets) {
			right -= widget.getWidth();
			widget.setPosition(right, y);
			right -= GAP;
		}
		for (var widget : widgets) widget.render(gui, mouseX, mouseY, delta);
	}
}
