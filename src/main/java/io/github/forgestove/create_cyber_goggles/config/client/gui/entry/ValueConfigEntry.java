package io.github.forgestove.create_cyber_goggles.config.client.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.client.ClientLockManager;
import io.github.forgestove.create_cyber_goggles.config.client.Translation;
import io.github.forgestove.create_cyber_goggles.config.client.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.client.gui.util.GuiUtil;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
	public final Button lockButton;
	public final ConfigCategoryTab<C> tab;
	public final List<AbstractWidget> children = new ArrayList<>();
	public final ValueConfigNode<C, V> valueNode;
	public final Component label;
	@Nullable public final List<FormattedCharSequence> tooltip;
	public List<FormattedCharSequence> tooltipWithError;
	@Nullable public Component validationError;
	public boolean hasChanged;
	public ValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, V> node) {
		valueNode = node;
		this.tab = tab;
		label = valueNode.getTitle().copy().withStyle(ChatFormatting.WHITE);
		var font = tab.getMinecraft().font;
		tooltip = valueNode.getTooltip() == null ? null : font.split(valueNode.getTooltip(), 350);
		tooltipWithError = getTooltipWithError();
		resetButton = Button.builder(Translation.RESET_LABEL, b -> resetToDefault())
			.size(Math.max(font.width(Translation.RESET_LABEL) + 6, SIZE), SIZE)
			.tooltip(Tooltip.create(Translation.RESET_TOOLTIP))
			.build();
		undoButton = Button.builder(Translation.UNDO_LABEL, b -> resetToActive())
			.size(Math.max(font.width(Translation.UNDO_LABEL) + 6, SIZE), SIZE)
			.tooltip(Tooltip.create(Translation.UNDO_TOOLTIP))
			.build();
		resetButton.active = !valueNode.isDefaultValue(this.tab.getConfig());
		undoButton.active = !valueNode.isActiveValue(this.tab.getConfig());
		lockButton = Button.builder(Translation.UNLOCK_LABEL, b -> onLockToggle())
			.size(Math.max(font.width(Translation.UNLOCK_LABEL) + 6, SIZE), SIZE)
			.tooltip(Tooltip.create(Translation.UNLOCK_TOOLTIP))
			.build();
		lockButton.active = canLock();
		lockButton.visible = shouldShowLockButton();
		children.add(resetButton);
		children.add(undoButton);
		children.add(lockButton);
	}
	public List<FormattedCharSequence> getTooltipWithError() {
		if (!hasError()) return tooltip;
		var errorTooltip = new ArrayList<FormattedCharSequence>();
		if (tooltip != null) errorTooltip.addAll(tooltip);
		if (validationError != null) errorTooltip.add(validationError.copy().withStyle(ChatFormatting.RED).getVisualOrderText());
		return errorTooltip;
	}
	public void resetToDefault() {
		ClientLockManager.clearPendingLock(valueNode.getPath());
		valueNode.resetToDefault();
		tab.getScreen().refresh();
	}
	public void resetToActive() {
		ClientLockManager.clearPendingLock(valueNode.getPath());
		valueNode.resetToActive(tab.getConfig());
		tab.getScreen().refresh();
	}
	/** Toggle pending lock state (deferred until save). */
	private void onLockToggle() {
		var path = valueNode.getPath();
		var shouldLock = !isLocked(); // effective state, considering pending
		ClientLockManager.setPendingLock(path, shouldLock);
		tab.getScreen().refresh();
	}
	private boolean canLock() {
		var mc = Minecraft.getInstance();
		return mc.player != null && mc.player.hasPermissions(2);
	}
	private boolean shouldShowLockButton() {
		var mc = Minecraft.getInstance();
		if (mc.player == null) return false;
		if (mc.isSingleplayer()) return false;
		return ClientLockManager.hasReceivedSync();
	}
	/**
	 * Check the effective lock state of this entry.
	 * Considers pending lock actions first, then falls back to server-confirmed locks.
	 */
	public boolean isLocked() {
		var path = valueNode.getPath();
		var pending = ClientLockManager.getPendingLock(path);
		if (pending != null) return pending;
		return ClientLockManager.isLocked(path);
	}
	public V getValue() {
		return valueNode.getEditingValue(tab.getConfig());
	}
	public void setValue(V value) {
		valueNode.setEditingValue(value);
		tab.getScreen().refresh();
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
	public void refresh() {
		var locked = isLocked();
		var hasPendingLock = ClientLockManager.getPendingLock(valueNode.getPath()) != null;
		resetButton.active = !locked && !valueNode.isDefaultValue(tab.getConfig());
		undoButton.active = !locked && !valueNode.isActiveValue(tab.getConfig());
		validationError = valueNode.validate(tab.getConfig());
		hasChanged = hasPendingLock || !locked && !valueNode.isActiveValue(tab.getConfig());
		tooltipWithError = getTooltipWithError();
		lockButton.visible = shouldShowLockButton();
		lockButton.active = lockButton.visible && canLock();
		if (locked) {
			lockButton.setMessage(Translation.LOCKED_LABEL);
			lockButton.setTooltip(Tooltip.create(Translation.LOCKED_TOOLTIP));
		} else {
			lockButton.setMessage(Translation.UNLOCK_LABEL);
			lockButton.setTooltip(Tooltip.create(Translation.UNLOCK_TOOLTIP));
		}
	}
	@Override
	public boolean hasError() {
		return validationError != null;
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
		var indent = getIndent();
		var label = GuiUtil.styleAsState(this.label, hasError(), hasChanged);
		if (isLocked()) label = label.copy().withStyle(ChatFormatting.GRAY, ChatFormatting.STRIKETHROUGH);
		gui.drawString(tab.getMinecraft().font, label.getVisualOrderText(), x + indent, y + 5, -1, false);
		var right = x + width;
		for (var widget : widgets) {
			if (!widget.visible) continue;
			right -= widget.getWidth();
			widget.setPosition(right, y);
			right -= GAP;
		}
		for (var widget : widgets) if (widget.visible) widget.render(gui, mouseX, mouseY, delta);
	}
}
