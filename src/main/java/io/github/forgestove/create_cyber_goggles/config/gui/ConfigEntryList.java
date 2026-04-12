package io.github.forgestove.create_cyber_goggles.config.gui;
import io.github.forgestove.create_cyber_goggles.config.Translation;
import io.github.forgestove.create_cyber_goggles.config.gui.entry.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.*;
public final class ConfigEntryList extends ContainerObjectSelectionList<ConfigEntry> {
	private static final float ANIMATION_SPEED = 0.4f;
	private final ConfigCategoryTab<?> tab;
	@Nullable private EnumValueConfigEntry<?> expandedDropdown;
	// Highlight animation state
	private float highlightY;
	private float highlightTargetY;
	private float highlightAlpha;
	private int highlightHeight;
	@Nullable private ConfigEntry lastHoveredEntry;
	public ConfigEntryList(
		ConfigCategoryTab<?> tab,
		Minecraft minecraft,
		int width,
		int contentHeight,
		int headerHeight,
		int itemSpacing,
		@NotNull Iterable<ConfigEntry> entries
	) {
		super(minecraft, width, contentHeight, headerHeight, itemSpacing);
		this.tab = tab;
		entries.forEach(this::addEntry);
	}
	@Override
	public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		// Track which dropdown is expanded
		expandedDropdown = null;
		for (var entry : children())
			if (entry instanceof EnumValueConfigEntry<?> enumEntry && enumEntry.isExpanded()) {
				expandedDropdown = enumEntry;
				break;
			}
		// Render dropdown overlay on top of everything (outside scissor)
		var showTooltip = expandedDropdown == null;
		if (expandedDropdown != null) {
			expandedDropdown.renderDropdownOverlay(guiGraphics, mouseX, mouseY);
			// Don't show tooltips when mouse is over the dropdown
			showTooltip = !expandedDropdown.isMouseOverDropdown(mouseX, mouseY);
		}
		if (showTooltip) {
			renderHighlight(guiGraphics);
			updateHighlightAnimation(delta);
		}
		super.renderWidget(guiGraphics, mouseX, mouseY, delta);
		// Tooltips
		if (!showTooltip) return;
		var entry = getHovered();
		if (entry == null) return;
		if (entry instanceof ValueConfigEntry<?, ?, ?> valueEntry) if (valueEntry.resetButton.isHovered()) {
			tab.getScreen().setTooltipForNextRenderPass(Translation.RESET_TOOLTIP);
			return;
		} else if (valueEntry.undoButton.isHovered()) {
			tab.getScreen().setTooltipForNextRenderPass(Translation.UNDO_TOOLTIP);
			return;
		} else if (valueEntry instanceof ColorValueConfigEntry<?> colorEntry && colorEntry.pickerButton.isHovered()) {
			tab.getScreen().setTooltipForNextRenderPass(Translation.COLOR_PICKER_TOOLTIP);
			return;
		}
		if (entry.getTooltip() != null) tab.getScreen().setTooltipForNextRenderPass(entry.getTooltip());
	}
	private void updateHighlightAnimation(float delta) {
		var hoveredEntry = getHovered();
		if (hoveredEntry != null) {
			var index = children().indexOf(hoveredEntry);
			if (index >= 0) {
				var entryTop = getRowTop(index);
				highlightTargetY = entryTop;
				highlightHeight = itemHeight;
				// Fade in
				highlightAlpha = Mth.lerp(ANIMATION_SPEED * delta, highlightAlpha, 0.95F);
				// Initialize position if first hover
				if (highlightY < 0 || lastHoveredEntry == null) highlightY = entryTop;
			}
			lastHoveredEntry = hoveredEntry;
		} else highlightAlpha = Mth.lerp(ANIMATION_SPEED * delta, highlightAlpha, 0.0f); // Fade out
		// Smooth position transition with snap to target
		if (!(highlightTargetY >= 0) || !(highlightY >= 0)) return;
		highlightY = Mth.lerp(ANIMATION_SPEED * delta * 2, highlightY, highlightTargetY);
		// Snap to target when close enough
		if (Math.abs(highlightY - highlightTargetY) < 1.0f) highlightY = highlightTargetY;
	}
	private void renderHighlight(@NotNull GuiGraphics guiGraphics) {
		if (highlightAlpha <= 0.01f || highlightY < 0) return;
		var alpha = (int) (highlightAlpha * 48); // Max alpha 48 (0x30)
		var color = alpha << 24 | 0xFFFFFF;
		var left = getX();
		var right = getX() + getWidth();
		var offset = -1; // Move highlight up
		var top = (int) highlightY + offset;
		var bottom = top + highlightHeight;
		// Clip to visible area
		var visibleTop = getY();
		var visibleBottom = getY() + getHeight();
		if (top < visibleTop) top = visibleTop;
		if (bottom > visibleBottom) bottom = visibleBottom;
		if (top < bottom) guiGraphics.fill(left, top, right, bottom, color);
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// First check if expanded dropdown should handle the click
		if (expandedDropdown != null) {
			if (expandedDropdown.isMouseOverDropdown(mouseX, mouseY)) return expandedDropdown.handleDropdownClick(mouseX, mouseY);
			// Click outside dropdown closes it
			expandedDropdown.closeDropdown();
			expandedDropdown = null;
			// Don't process further if we just closed a dropdown
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (expandedDropdown != null) expandedDropdown.mouseReleased(mouseX, mouseY, button);
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (expandedDropdown != null) if (expandedDropdown.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		// First check if expanded dropdown should handle the scroll
		if (expandedDropdown != null && expandedDropdown.handleDropdownScroll(mouseX, mouseY, vertical)) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
	}
	@Override
	public int getRowWidth() {
		return width - 80;
	}
	public void refreshEntries() {
		children().forEach(ConfigEntry::refresh);
	}
	public boolean hasEntryError() {
		return children().stream().anyMatch(ConfigEntry::hasError);
	}
	public boolean handleKeyCapture(int keyCode) {
		for (var entry : children())
			if (entry instanceof KeybindValueConfigEntry<?> keybindEntry && keybindEntry.handleCaptureKey(keyCode)) return true;
		return false;
	}
	public boolean handleMouseCapture(int button) {
		for (var entry : children())
			if (entry instanceof KeybindValueConfigEntry<?> keybindEntry && keybindEntry.handleCaptureMouse(button)) return true;
		return false;
	}
	public boolean isCapturingKeybind() {
		for (var entry : children())
			if (entry instanceof KeybindValueConfigEntry<?> keybindEntry && keybindEntry.isCapturing()) return true;
		return false;
	}
}
