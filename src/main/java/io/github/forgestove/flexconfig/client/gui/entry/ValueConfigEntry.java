package io.github.forgestove.flexconfig.client.gui.entry;
import io.github.forgestove.flexconfig.client.*;
import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
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
	public final Button lockButton;
	public final ConfigCategoryTab<C, V> tab;
	public final List<AbstractWidget> children = new ArrayList<>();
	public final ValueConfigNode<C, V> node;
	public final Component label;
	@Nullable public final List<FormattedCharSequence> tooltip;
	public List<FormattedCharSequence> tooltipWithError;
	@Nullable public Component validationError;
	public boolean hasChanged;
	public ValueConfigEntry(ConfigCategoryTab<C, V> tab, ValueConfigNode<C, V> node) {
		this.node = node;
		this.tab = tab;
		label = this.node.getTitle().copy().withStyle(ChatFormatting.WHITE);
		var font = ClientUtil.mc.font;
		tooltip = this.node.getTooltip() == null ? null : font.split(this.node.getTooltip(), 350);
		tooltipWithError = getTooltipWithError();
		resetButton = Button.builder(Translation.RESET_LABEL, b -> resetToDefault())
			.size(Math.max(font.width(Translation.RESET_LABEL) + 6, SIZE), SIZE)
			.tooltip(Tooltip.create(Translation.RESET_TOOLTIP))
			.build();
		undoButton = Button.builder(Translation.UNDO_LABEL, b -> resetToActive())
			.size(Math.max(font.width(Translation.UNDO_LABEL) + 6, SIZE), SIZE)
			.tooltip(Tooltip.create(Translation.UNDO_TOOLTIP))
			.build();
		resetButton.active = !this.node.isDefaultValue(this.tab.config);
		undoButton.active = !this.node.isActiveValue(this.tab.config);
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
		if (isLocked()) return;
		var path = node.getPath();
		var pending = ClientLockManager.getPendingLock(path);
		if (pending != Boolean.FALSE) ClientLockManager.clearPendingLock(path);
		node.resetToDefault();
		tab.screen.refresh();
	}
	public void resetToActive() {
		var path = node.getPath();
		var pending = ClientLockManager.getPendingLock(path);
		if (pending != Boolean.FALSE) ClientLockManager.clearPendingLock(path);
		node.resetToActive(tab.config);
		tab.screen.refresh();
	}
	/** 切换待处理的锁定状态（延迟至保存时执行）。 */
	private void onLockToggle() {
		var path = node.getPath();
		var shouldLock = !isLocked(); // effective state, considering pending
		ClientLockManager.setPendingLock(path, shouldLock);
		tab.screen.refresh();
	}
	private boolean canLock() {
		return ClientUtil.mc.player != null && ClientUtil.mc.player.hasPermissions(2);
	}
	private boolean shouldShowLockButton() {
		if (ClientUtil.mc.player == null) return false;
		if (ClientUtil.mc.isSingleplayer()) return false;
		return ClientLockManager.hasReceivedSync();
	}
	@Override
	public boolean hasError() {
		return validationError != null;
	}
	/**
	 * 检查此条目的有效锁定状态。
	 * 首先考虑待处理的锁定操作，然后回退到服务器确认的锁定。
	 */
	public boolean isLocked() {
		var path = node.getPath();
		var pending = ClientLockManager.getPendingLock(path);
		if (pending != null) return pending;
		return ClientLockManager.isLocked(tab.screen.root.modId(), path);
	}
	public V getValue() {
		return node.getEditingValue(tab.config);
	}
	public void setValue(V value) {
		node.setEditingValue(value);
		tab.screen.refresh();
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
	@Override
	public List<FormattedCharSequence> getTooltip() {
		return hasError() ? tooltipWithError : tooltip;
	}
	@Override
	public void refresh() {
		var locked = isLocked();
		var hasPendingLock = Boolean.TRUE.equals(ClientLockManager.getPendingLock(node.getPath()));
		resetButton.active = !locked && !node.isDefaultValue(tab.config);
		undoButton.active = !locked && !node.isActiveValue(tab.config);
		validationError = node.validate(tab.config);
		hasChanged = hasPendingLock || !locked && !node.isActiveValue(tab.config);
		tooltipWithError = getTooltipWithError();
		lockButton.visible = shouldShowLockButton();
		lockButton.active = lockButton.visible && canLock();
		if (locked) {
			lockButton.setMessage(Translation.LOCKED_LABEL.copy().withStyle(ChatFormatting.RED));
			lockButton.setTooltip(Tooltip.create(Translation.LOCKED_TOOLTIP));
		} else {
			lockButton.setMessage(Translation.UNLOCK_LABEL.copy().withStyle(ChatFormatting.GREEN));
			lockButton.setTooltip(Tooltip.create(Translation.UNLOCK_TOOLTIP));
		}
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
		var label = ConfigCategoryTab.styleAsState(this.label, hasError(), hasChanged);
		if (isLocked()) label = label.copy().withStyle(ChatFormatting.GRAY, ChatFormatting.STRIKETHROUGH);
		gui.drawString(ClientUtil.mc.font, label.getVisualOrderText(), x + indent, y + 5, -1, false);
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
