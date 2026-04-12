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
		enumValues = valueNode.getValueType().getEnumConstants();
		enumClassName = valueNode.getValueType().getSimpleName();
		dropdownButton = Button.builder(getDisplayComponent(getValue()), this::toggleDropdown).bounds(0, 0, 160, 20).build();
		children.add(dropdownButton);
	}
	private Component getDisplayComponent(Enum<?> value) {
		return Component.translatable(CCG.ID + ".config.enum." + enumClassName + "." + value.name());
	}
	private void toggleDropdown(Button button) {
		expanded = !expanded;
		if (!expanded) return;
		// Calculate available space and max visible options
		var availableHeight = screenBottom - dropdownY - DROPDOWN_PADDING * 2;
		maxVisibleOptions = Mth.clamp(availableHeight / OPTION_HEIGHT, 1, enumValues.length);
		// Scroll to show current selection
		var currentIndex = Arrays.asList(enumValues).indexOf(getValue());
		scrollOffset = Mth.clamp(currentIndex - maxVisibleOptions / 2, 0, getMaxScrollOffset());
	}
	private void selectValue(Enum<?> value) {
		setValue(value);
		expanded = false;
		dropdownButton.setMessage(getDisplayComponent(value));
	}
	public void closeDropdown() {
		expanded = false;
		isDraggingScrollbar = false;
	}
	private int getMaxScrollOffset() {
		return Math.max(0, enumValues.length - maxVisibleOptions);
	}
	private boolean needsScrollbar() {
		return enumValues.length > maxVisibleOptions;
	}
	private int getDropdownHeight() {
		return maxVisibleOptions * OPTION_HEIGHT + DROPDOWN_PADDING * 2;
	}
	@Override
	public void refresh() {
		super.refresh();
		dropdownButton.setMessage(getDisplayComponent(getValue()));
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
		renderLabel(guiGraphics, x, y, entryWidth);
		dropdownWidth = 160 - resetButton.getWidth() - 2 - undoButton.getWidth() - 2;
		dropdownButton.setWidth(dropdownWidth);
		if (tab.getMinecraft().font.isBidirectional()) {
			undoButton.setX(x);
			resetButton.setX(x + undoButton.getWidth() + 2);
			dropdownButton.setX(x + undoButton.getWidth() + 2 + resetButton.getWidth() + 2);
		} else {
			undoButton.setX(x + entryWidth - undoButton.getWidth());
			resetButton.setX(undoButton.getX() - resetButton.getWidth() - 2);
			dropdownButton.setX(resetButton.getX() - dropdownButton.getWidth() - 2);
		}
		undoButton.setY(y);
		resetButton.setY(y);
		dropdownButton.setY(y);
		dropdownX = dropdownButton.getX();
		dropdownY = y + 22;
		// Recalculate max visible options when position changes
		if (expanded) {
			// Calculate screen bottom for dropdown height limit
			screenBottom = tab.getScreen().height - tab.getScreen().getFooterHeight();
			var availableHeight = screenBottom - dropdownY - DROPDOWN_PADDING * 2;
			maxVisibleOptions = Mth.clamp(availableHeight / OPTION_HEIGHT, 1, enumValues.length);
			scrollOffset = Mth.clamp(scrollOffset, 0, getMaxScrollOffset());
		}
		dropdownButton.render(guiGraphics, mouseX, mouseY, delta);
		resetButton.render(guiGraphics, mouseX, mouseY, delta);
		undoButton.render(guiGraphics, mouseX, mouseY, delta);
	}
	/** Render the dropdown overlay - called after all entries are rendered */
	public void renderDropdownOverlay(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (!expanded) return;
		var pose = guiGraphics.pose();
		pose.pushPose();
		pose.translate(0, 0, 100); // Bring to front
		var dropdownHeight = getDropdownHeight();
		var contentWidth = needsScrollbar() ? dropdownWidth - SCROLLBAR_WIDTH - 2 : dropdownWidth;
		// Draw background with border
		guiGraphics.fill(dropdownX - 1, dropdownY - 1, dropdownX + dropdownWidth + 1, dropdownY + dropdownHeight + 1, 0xFF000000);
		guiGraphics.fill(dropdownX, dropdownY, dropdownX + dropdownWidth, dropdownY + dropdownHeight, 0xFF2D2D2D);
		// Draw options
		for (var i = 0; i < maxVisibleOptions; i++) {
			var optionIndex = i + scrollOffset;
			if (optionIndex >= enumValues.length) break;
			var enumValue = enumValues[optionIndex];
			var optionY = dropdownY + DROPDOWN_PADDING + i * OPTION_HEIGHT;
			var isHovered = mouseX >= dropdownX
				&& mouseX < dropdownX + contentWidth
				&& mouseY >= optionY
				&& mouseY < optionY + OPTION_HEIGHT;
			var isSelected = enumValue == getValue();
			// Background
			if (isSelected) guiGraphics.fill(dropdownX + 1, optionY, dropdownX + contentWidth - 1, optionY + OPTION_HEIGHT - 1,
				0xFF3366BB);
			else if (isHovered)
				guiGraphics.fill(dropdownX + 1, optionY, dropdownX + contentWidth - 1, optionY + OPTION_HEIGHT - 1, 0xFF404040);
			// Text
			var text = getDisplayComponent(enumValue);
			var textColor = isSelected ? 0xFFFFFFFF : isHovered ? 0xFFFFFF00 : 0xFFE0E0E0;
			guiGraphics.drawString(tab.getMinecraft().font, text, dropdownX + 4, optionY + 4, textColor, false);
		}
		// Draw scrollbar if needed
		if (!needsScrollbar()) {
			pose.popPose();
			return;
		}
		var scrollbarX = dropdownX + dropdownWidth - SCROLLBAR_WIDTH - 1;
		var scrollbarTrackHeight = dropdownHeight - DROPDOWN_PADDING * 2;
		var scrollbarHeight = Math.max(15, scrollbarTrackHeight * maxVisibleOptions / enumValues.length);
		var maxScrollOffset = getMaxScrollOffset();
		var scrollbarY = dropdownY + DROPDOWN_PADDING + (
			maxScrollOffset > 0 ? (scrollbarTrackHeight - scrollbarHeight) * scrollOffset / maxScrollOffset : 0
		);
		// Scrollbar track
		guiGraphics.fill(
			scrollbarX,
			dropdownY + DROPDOWN_PADDING,
			scrollbarX + SCROLLBAR_WIDTH,
			dropdownY + dropdownHeight - DROPDOWN_PADDING,
			0xFF1A1A1A
		);
		// Scrollbar thumb
		var scrollbarHovered = mouseX >= scrollbarX
			&& mouseX < scrollbarX + SCROLLBAR_WIDTH
			&& mouseY >= scrollbarY
			&& mouseY < scrollbarY + scrollbarHeight;
		var thumbColor = scrollbarHovered || isDraggingScrollbar ? 0xFF888888 : 0xFF555555;
		guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + SCROLLBAR_WIDTH, scrollbarY + scrollbarHeight, thumbColor);
		pose.popPose();
	}
	public boolean isExpanded() {
		return expanded;
	}
	public boolean isMouseOverDropdown(double mouseX, double mouseY) {
		if (!expanded) return false;
		var dropdownHeight = getDropdownHeight();
		return mouseX >= dropdownX - 1
			&& mouseX < dropdownX + dropdownWidth + 1
			&& mouseY >= dropdownY - 1
			&& mouseY < dropdownY + dropdownHeight + 1;
	}
	public boolean handleDropdownClick(double mouseX, double mouseY) {
		if (!expanded) return false;
		var dropdownHeight = getDropdownHeight();
		var contentWidth = needsScrollbar() ? dropdownWidth - SCROLLBAR_WIDTH - 2 : dropdownWidth;
		// Check scrollbar click
		if (needsScrollbar()) {
			var scrollbarX = dropdownX + dropdownWidth - SCROLLBAR_WIDTH - 1;
			if (mouseX >= scrollbarX
				&& mouseX < scrollbarX + SCROLLBAR_WIDTH
				&& mouseY >= dropdownY
				&& mouseY < dropdownY + dropdownHeight) {
				isDraggingScrollbar = true;
				updateScrollFromMouse(mouseY);
				return true;
			}
		}
		// Check option click
		for (var i = 0; i < maxVisibleOptions; i++) {
			var optionIndex = i + scrollOffset;
			if (optionIndex >= enumValues.length) break;
			var optionY = dropdownY + DROPDOWN_PADDING + i * OPTION_HEIGHT;
			if (mouseX >= dropdownX && mouseX < dropdownX + contentWidth && mouseY >= optionY && mouseY < optionY + OPTION_HEIGHT) {
				selectValue(enumValues[optionIndex]);
				return true;
			}
		}
		return false;
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (expanded && isMouseOverDropdown(mouseX, mouseY)) return handleDropdownClick(mouseX, mouseY);
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		isDraggingScrollbar = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!isDraggingScrollbar) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		updateScrollFromMouse(mouseY);
		return true;
	}
	private void updateScrollFromMouse(double mouseY) {
		var dropdownHeight = getDropdownHeight();
		var scrollbarTrackHeight = dropdownHeight - DROPDOWN_PADDING * 2;
		var scrollbarHeight = Math.max(15, scrollbarTrackHeight * maxVisibleOptions / enumValues.length);
		var relativeY = mouseY - dropdownY - DROPDOWN_PADDING - scrollbarHeight / 2.0;
		double scrollRange = scrollbarTrackHeight - scrollbarHeight;
		if (scrollRange > 0)
			scrollOffset = Mth.clamp((int) Math.round(relativeY / scrollRange * getMaxScrollOffset()), 0, getMaxScrollOffset());
	}
	public boolean handleDropdownScroll(double mouseX, double mouseY, double vertical) {
		if (!expanded || !isMouseOverDropdown(mouseX, mouseY)) return false;
		scrollOffset = Mth.clamp(scrollOffset - (int) vertical, 0, getMaxScrollOffset());
		return true;
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (handleDropdownScroll(mouseX, mouseY, vertical)) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
	}
}
