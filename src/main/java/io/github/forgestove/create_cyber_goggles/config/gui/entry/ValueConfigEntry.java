package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.Translation;
import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
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
	protected final ConfigCategoryTab<C> tab;
	protected final List<AbstractWidget> children = new ArrayList<>();
	protected final ValueConfigNode<C, T, V> valueNode;
	private final Component label;
	private final Component labelChanged;
	private final Component labelError;
	private final Component labelErrorChanged;
	@Nullable private final List<FormattedCharSequence> tooltip;
	private List<FormattedCharSequence> tooltipWithError;
	@Nullable private Component validationError;
	private boolean hasChanged;
	protected ValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, T, V> valueNode) {
		this.tab = tab;
		label = valueNode.getTitle().copy().withStyle(ChatFormatting.WHITE);
		labelChanged = label.copy().withStyle(ChatFormatting.ITALIC, ChatFormatting.YELLOW);
		labelError = label.copy().withStyle(ChatFormatting.RED);
		labelErrorChanged = label.copy().withStyle(ChatFormatting.ITALIC, ChatFormatting.RED);
		tooltip = valueNode.getTooltip() == null ? null : tab.getMinecraft().font.split(valueNode.getTooltip(), 350);
		tooltipWithError = getTooltipWithError();
		this.valueNode = valueNode;
		resetButton = Button.builder(Translation.RESET_LABEL, b -> resetToDefault())
			.bounds(0, 0, Math.max(tab.getMinecraft().font.width(Translation.RESET_LABEL) + 6, 20), 20)
			.build();
		resetButton.active = !valueNode.isDefaultValue(this.tab.getConfig());
		children.add(resetButton);
		undoButton = Button.builder(Translation.UNDO_LABEL, b -> resetToActive())
			.bounds(0, 0, Math.max(tab.getMinecraft().font.width(Translation.UNDO_LABEL) + 6, 20), 20)
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
	private List<FormattedCharSequence> getTooltipWithError() {
		if (hasError()) {
			List<FormattedCharSequence> errorTooltip = new ArrayList<>();
			if (tooltip != null) errorTooltip.addAll(tooltip);
			assert validationError != null;
			errorTooltip.add(validationError.copy().withStyle(ChatFormatting.RED).getVisualOrderText());
			return errorTooltip;
		}
		return tooltip;
	}
	protected void renderLabel(GuiGraphics guiGraphics, int x, int y) {
		Component l;
		if (hasError()) l = hasChanged ? labelErrorChanged : labelError;
		else l = hasChanged ? labelChanged : label;
		guiGraphics.drawString(tab.getMinecraft().font, l.getVisualOrderText(), x, y + 5, -1, false);
	}
}
