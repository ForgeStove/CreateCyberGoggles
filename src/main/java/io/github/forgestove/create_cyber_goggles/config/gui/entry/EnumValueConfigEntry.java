package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
public final class EnumValueConfigEntry<C> extends ValueConfigEntry<C, Enum<?>, Enum<?>> {
	private static final int OPTION_HEIGHT = 18;
	private static final int SCROLLBAR_WIDTH = 6;
	private static final int DROPDOWN_PADDING = 2;
	private final Button dropdownButton;
	private final Enum<?>[] enumValues;
	private final String enumClassName;
	private boolean expanded = false;
	private int scrollOffset = 0;
	private int dropdownX;
	private int dropdownY;
	private int dropdownWidth;
	private int maxVisibleOptions;
	private int screenBottom;
	private boolean isDraggingScrollbar = false;
	public EnumValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, Enum<?>, Enum<?>> valueNode) {
		super(tab, valueNode);
		this.enumValues = valueNode.getValueType().getEnumConstants();
		this.enumClassName = valueNode.getValueType().getSimpleName();
		this.dropdownButton =
			Button.builder(this.getDisplayComponent(this.getValue()), this::toggleDropdown).bounds(0, 0, 160, 20).build();
		this.children.add(this.dropdownButton);
	}
	private Component getDisplayComponent(Enum<?> value) {
		return Component.translatable(CCG.ID + ".config.enum." + this.enumClassName + "." + value.name());
	}
	private void toggleDropdown(Button button) {
		this.expanded = !this.expanded;
		if (this.expanded) {
			// Calculate available space and max visible options
			var availableHeight = this.screenBottom - this.dropdownY - DROPDOWN_PADDING * 2;
			this.maxVisibleOptions = Math.max(1, Math.min(this.enumValues.length, availableHeight / OPTION_HEIGHT));
			// Scroll to show current selection
			var currentIndex = Arrays.asList(this.enumValues).indexOf(this.getValue());
			this.scrollOffset = Mth.clamp(currentIndex - this.maxVisibleOptions / 2, 0, this.getMaxScrollOffset());
		}
	}
	private void selectValue(Enum<?> value) {
		this.setValue(value);
		this.expanded = false;
		this.dropdownButton.setMessage(this.getDisplayComponent(value));
	}
	public void closeDropdown() {
		this.expanded = false;
		this.isDraggingScrollbar = false;
	}
	private int getMaxScrollOffset() {
		return Math.max(0, this.enumValues.length - this.maxVisibleOptions);
	}
	private boolean needsScrollbar() {
		return this.enumValues.length > this.maxVisibleOptions;
	}
	private int getDropdownHeight() {
		return this.maxVisibleOptions * OPTION_HEIGHT + DROPDOWN_PADDING * 2;
	}
	@Override
	public void refresh() {
		super.refresh();
		this.dropdownButton.setMessage(this.getDisplayComponent(this.getValue()));
	}
	@Override
	public void render(
		@NotNull GuiGraphics guiGraphics,
		int index,
		int y,
		int x,
		int entryWidth,
		int entryHeight,
		int mouseX,
		int mouseY,
		boolean hovered,
		float delta
	) {
		this.renderLabel(guiGraphics, x, y, entryWidth);
		this.dropdownWidth = 160 - this.resetButton.getWidth() - 2 - this.undoButton.getWidth() - 2;
		this.dropdownButton.setWidth(this.dropdownWidth);
		// Calculate screen bottom for dropdown height limit
		this.screenBottom = this.tab.getScreen().height - this.tab.getScreen().getFooterHeight();
		if (this.tab.getMinecraft().font.isBidirectional()) {
			this.undoButton.setX(x);
			this.undoButton.setY(y);
			this.resetButton.setX(x + this.undoButton.getWidth() + 2);
			this.resetButton.setY(y);
			this.dropdownButton.setX(x + this.undoButton.getWidth() + 2 + this.resetButton.getWidth() + 2);
		} else {
			this.undoButton.setX(x + entryWidth - this.undoButton.getWidth());
			this.undoButton.setY(y);
			this.resetButton.setX(this.undoButton.getX() - this.resetButton.getWidth() - 2);
			this.resetButton.setY(y);
			this.dropdownButton.setX(this.resetButton.getX() - this.dropdownButton.getWidth() - 2);
		}
		this.dropdownButton.setY(y);
		this.dropdownX = this.dropdownButton.getX();
		this.dropdownY = y + 22;
		// Recalculate max visible options when position changes
		if (this.expanded) {
			var availableHeight = this.screenBottom - this.dropdownY - DROPDOWN_PADDING * 2;
			this.maxVisibleOptions = Math.max(1, Math.min(this.enumValues.length, availableHeight / OPTION_HEIGHT));
			this.scrollOffset = Mth.clamp(this.scrollOffset, 0, this.getMaxScrollOffset());
		}
		this.dropdownButton.render(guiGraphics, mouseX, mouseY, delta);
		this.resetButton.render(guiGraphics, mouseX, mouseY, delta);
		this.undoButton.render(guiGraphics, mouseX, mouseY, delta);
	}
	/** Render the dropdown overlay - called after all entries are rendered */
	public void renderDropdownOverlay(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (!this.expanded) return;
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0, 0, 100); // Bring to front
		var dropdownHeight = this.getDropdownHeight();
		var contentWidth = this.needsScrollbar() ? this.dropdownWidth - SCROLLBAR_WIDTH - 2 : this.dropdownWidth;
		// Draw background with border
		guiGraphics.fill(
			this.dropdownX - 1,
			this.dropdownY - 1,
			this.dropdownX + this.dropdownWidth + 1,
			this.dropdownY + dropdownHeight + 1,
			0xFF000000
		);
		guiGraphics.fill(this.dropdownX, this.dropdownY, this.dropdownX + this.dropdownWidth, this.dropdownY + dropdownHeight, 0xFF2D2D2D);
		// Draw options
		for (var i = 0; i < this.maxVisibleOptions; i++) {
			var optionIndex = i + this.scrollOffset;
			if (optionIndex >= this.enumValues.length) break;
			var enumValue = this.enumValues[optionIndex];
			var optionY = this.dropdownY + DROPDOWN_PADDING + i * OPTION_HEIGHT;
			var isHovered = mouseX >= this.dropdownX
				&& mouseX < this.dropdownX + contentWidth
				&& mouseY >= optionY
				&& mouseY < optionY + OPTION_HEIGHT;
			var isSelected = enumValue == this.getValue();
			// Background
			if (isSelected)
				guiGraphics.fill(this.dropdownX + 1, optionY, this.dropdownX + contentWidth - 1, optionY + OPTION_HEIGHT - 1, 0xFF3366BB);
			else if (isHovered)
				guiGraphics.fill(this.dropdownX + 1, optionY, this.dropdownX + contentWidth - 1, optionY + OPTION_HEIGHT - 1, 0xFF404040);
			// Text
			var text = this.getDisplayComponent(enumValue);
			var textColor = isSelected ? 0xFFFFFFFF : isHovered ? 0xFFFFFF00 : 0xFFE0E0E0;
			guiGraphics.drawString(this.tab.getMinecraft().font, text, this.dropdownX + 4, optionY + 4, textColor, false);
		}
		// Draw scrollbar if needed
		if (this.needsScrollbar()) {
			var scrollbarX = this.dropdownX + this.dropdownWidth - SCROLLBAR_WIDTH - 1;
			var scrollbarTrackHeight = dropdownHeight - DROPDOWN_PADDING * 2;
			var scrollbarHeight = Math.max(15, scrollbarTrackHeight * this.maxVisibleOptions / this.enumValues.length);
			var maxScrollOffset = this.getMaxScrollOffset();
			var scrollbarY = this.dropdownY + DROPDOWN_PADDING + (
				maxScrollOffset > 0 ? (scrollbarTrackHeight - scrollbarHeight) * this.scrollOffset / maxScrollOffset : 0
			);
			// Scrollbar track
			guiGraphics.fill(
				scrollbarX,
				this.dropdownY + DROPDOWN_PADDING,
				scrollbarX + SCROLLBAR_WIDTH,
				this.dropdownY + dropdownHeight - DROPDOWN_PADDING,
				0xFF1A1A1A
			);
			// Scrollbar thumb
			var scrollbarHovered = mouseX >= scrollbarX
				&& mouseX < scrollbarX + SCROLLBAR_WIDTH
				&& mouseY >= scrollbarY
				&& mouseY < scrollbarY + scrollbarHeight;
			var thumbColor = scrollbarHovered || this.isDraggingScrollbar ? 0xFF888888 : 0xFF555555;
			guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + SCROLLBAR_WIDTH, scrollbarY + scrollbarHeight, thumbColor);
		}
		guiGraphics.pose().popPose();
	}
	public boolean isExpanded() {
		return this.expanded;
	}
	public boolean isMouseOverDropdown(double mouseX, double mouseY) {
		if (!this.expanded) return false;
		var dropdownHeight = this.getDropdownHeight();
		return mouseX >= this.dropdownX - 1
			&& mouseX < this.dropdownX + this.dropdownWidth + 1
			&& mouseY >= this.dropdownY - 1
			&& mouseY < this.dropdownY + dropdownHeight + 1;
	}
	public boolean handleDropdownClick(double mouseX, double mouseY) {
		if (!this.expanded) return false;
		var dropdownHeight = this.getDropdownHeight();
		var contentWidth = this.needsScrollbar() ? this.dropdownWidth - SCROLLBAR_WIDTH - 2 : this.dropdownWidth;
		// Check scrollbar click
		if (this.needsScrollbar()) {
			var scrollbarX = this.dropdownX + this.dropdownWidth - SCROLLBAR_WIDTH - 1;
			if (mouseX >= scrollbarX
				&& mouseX < scrollbarX + SCROLLBAR_WIDTH
				&& mouseY >= this.dropdownY
				&& mouseY < this.dropdownY + dropdownHeight) {
				this.isDraggingScrollbar = true;
				this.updateScrollFromMouse(mouseY);
				return true;
			}
		}
		// Check option click
		for (var i = 0; i < this.maxVisibleOptions; i++) {
			var optionIndex = i + this.scrollOffset;
			if (optionIndex >= this.enumValues.length) break;
			var optionY = this.dropdownY + DROPDOWN_PADDING + i * OPTION_HEIGHT;
			if (mouseX >= this.dropdownX
				&& mouseX < this.dropdownX + contentWidth
				&& mouseY >= optionY
				&& mouseY < optionY + OPTION_HEIGHT) {
				this.selectValue(this.enumValues[optionIndex]);
				return true;
			}
		}
		return false;
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.expanded && this.isMouseOverDropdown(mouseX, mouseY)) return this.handleDropdownClick(mouseX, mouseY);
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		this.isDraggingScrollbar = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (this.isDraggingScrollbar) {
			this.updateScrollFromMouse(mouseY);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}
	private void updateScrollFromMouse(double mouseY) {
		var dropdownHeight = this.getDropdownHeight();
		var scrollbarTrackHeight = dropdownHeight - DROPDOWN_PADDING * 2;
		var scrollbarHeight = Math.max(15, scrollbarTrackHeight * this.maxVisibleOptions / this.enumValues.length);
		var relativeY = mouseY - this.dropdownY - DROPDOWN_PADDING - scrollbarHeight / 2.0;
		double scrollRange = scrollbarTrackHeight - scrollbarHeight;
		if (scrollRange > 0) this.scrollOffset = Mth.clamp(
			(int) Math.round(relativeY / scrollRange * this.getMaxScrollOffset()),
			0,
			this.getMaxScrollOffset()
		);
	}
	public boolean handleDropdownScroll(double mouseX, double mouseY, double vertical) {
		if (this.expanded && this.isMouseOverDropdown(mouseX, mouseY)) {
			this.scrollOffset = Mth.clamp(this.scrollOffset - (int) vertical, 0, this.getMaxScrollOffset());
			return true;
		}
		return false;
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double vertical) {
		if (this.handleDropdownScroll(mouseX, mouseY, vertical)) return true;
		return super.mouseScrolled(mouseX, mouseY, vertical);
	}
}
