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
		this.label = valueNode.getTitle().copy().withStyle(ChatFormatting.WHITE);
		this.labelChanged = label.copy().withStyle(ChatFormatting.ITALIC, ChatFormatting.YELLOW);
		this.labelError = label.copy().withStyle(ChatFormatting.RED);
		this.labelErrorChanged = label.copy().withStyle(ChatFormatting.ITALIC, ChatFormatting.RED);
		this.tooltip = valueNode.getTooltip() == null ? null : tab.getMinecraft().font.split(valueNode.getTooltip(), 350);
		this.tooltipWithError = this.getTooltipWithError();
		this.valueNode = valueNode;
		this.resetButton = Button.builder(Translation.RESET_LABEL, b -> this.resetToDefault())
			.bounds(0, 0, Math.max(tab.getMinecraft().font.width(Translation.RESET_LABEL) + 6, 20), 20)
			.build();
		this.resetButton.active = !valueNode.isDefaultValue(this.tab.getConfig());
		this.children.add(this.resetButton);
		this.undoButton = Button.builder(Translation.UNDO_LABEL, b -> this.resetToActive())
			.bounds(0, 0, Math.max(tab.getMinecraft().font.width(Translation.UNDO_LABEL) + 6, 20), 20)
			.build();
		this.undoButton.active = !valueNode.isActiveValue(this.tab.getConfig());
		this.children.add(this.undoButton);
	}
	public void resetToDefault() {
		this.valueNode.resetToDefault();
		this.tab.getScreen().refresh();
	}
	public void resetToActive() {
		this.valueNode.resetToActive(this.tab.getConfig());
		this.tab.getScreen().refresh();
	}
	public V getValue() {
		return this.valueNode.getEditingValue(this.tab.getConfig());
	}
	public void setValue(V value) {
		this.valueNode.setEditingValue(value);
		this.tab.getScreen().refresh();
	}
	public void refresh() {
		this.resetButton.active = !valueNode.isDefaultValue(this.tab.getConfig());
		this.undoButton.active = !valueNode.isActiveValue(this.tab.getConfig());
		this.validationError = this.valueNode.validate(this.tab.getConfig());
		this.hasChanged = !valueNode.isActiveValue(this.tab.getConfig());
		this.tooltipWithError = this.getTooltipWithError();
	}
	@Override
	public boolean hasError() {
		return this.validationError != null;
	}
	@NotNull
	@Override
	public List<? extends GuiEventListener> children() {
		return this.children;
	}
	@NotNull
	@Override
	public List<? extends NarratableEntry> narratables() {
		return this.children;
	}
	@Nullable
	public List<FormattedCharSequence> getTooltip() {
		return hasError() ? this.tooltipWithError : this.tooltip;
	}
	private List<FormattedCharSequence> getTooltipWithError() {
		if (hasError()) {
			List<FormattedCharSequence> errorTooltip = new ArrayList<>();
			if (this.tooltip != null) errorTooltip.addAll(this.tooltip);
			assert this.validationError != null;
			errorTooltip.add(this.validationError.copy().withStyle(ChatFormatting.RED).getVisualOrderText());
			return errorTooltip;
		}
		return this.tooltip;
	}
	protected void renderLabel(GuiGraphics guiGraphics, int x, int y, int entryWidth) {
		Component l;
		if (hasError()) l = this.hasChanged ? this.labelErrorChanged : this.labelError;
		else l = this.hasChanged ? this.labelChanged : this.label;
		if (this.tab.getMinecraft().font.isBidirectional()) x = x + entryWidth - this.tab.getMinecraft().font.width(l);
		guiGraphics.drawString(this.tab.getMinecraft().font, l.getVisualOrderText(), x, y + 5, -1, false);
	}
}
