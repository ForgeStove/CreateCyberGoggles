package io.github.forgestove.create_cyber_goggles.config.gui;
import io.github.forgestove.create_cyber_goggles.config.gui.entry.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.*;
public final class ConfigEntryList extends ContainerObjectSelectionList<ConfigEntry> {
	private static final float ANIMATION_SPEED = 0.4f;
	private final ConfigCategoryTab<?> tab;
	@Nullable private EnumValueConfigEntry<?, ?> expandedDropdown;
	// Highlight animation state
	private float highlightY = -1;
	private float highlightTargetY = -1;
	private float highlightAlpha = 0;
	private int highlightHeight = 24;
	@Nullable private ConfigEntry lastHoveredEntry;
	public ConfigEntryList(
		ConfigCategoryTab<?> tab,
		Minecraft minecraft,
		int width,
		int contentHeight,
		int headerHeight,
		int itemSpacing,
		Iterable<ConfigEntry> entries
	) {
		super(minecraft, width, contentHeight, headerHeight, itemSpacing);
		this.tab = tab;
		entries.forEach(this::addEntry);
	}
	@Override
	public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		// Update highlight animation
		this.updateHighlightAnimation(mouseX, mouseY, delta);
		// Render highlight before entries
		this.renderHighlight(guiGraphics);
		super.renderWidget(guiGraphics, mouseX, mouseY, delta);
		// Track which dropdown is expanded
		this.expandedDropdown = null;
		for (var entry : this.children())
			if (entry instanceof EnumValueConfigEntry<?, ?> enumEntry && enumEntry.isExpanded()) {
				this.expandedDropdown = enumEntry;
				break;
			}
		// Render dropdown overlay on top of everything (outside scissor)
		if (this.expandedDropdown != null) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0, 0, 100); // Bring to front
			this.expandedDropdown.renderDropdownOverlay(guiGraphics, mouseX, mouseY);
			guiGraphics.pose().popPose();
			// Don't show tooltips when mouse is over the dropdown
			if (this.expandedDropdown.isMouseOverDropdown(mouseX, mouseY)) return;
		}
		// Tooltips
		var entry = this.getHovered();
		if (entry != null) {
			if (entry instanceof ValueConfigEntry<?, ?, ?> valueEntry) if (valueEntry.resetButton.isHovered()) {
				this.tab.getScreen().setTooltipForNextRenderPass(ValueConfigEntry.RESET_BUTTON_TOOLTIP);
				return;
			} else if (valueEntry.undoButton.isHovered()) {
				this.tab.getScreen().setTooltipForNextRenderPass(ValueConfigEntry.UNDO_BUTTON_TOOLTIP);
				return;
			}
			if (entry.getTooltip() != null) this.tab.getScreen().setTooltipForNextRenderPass(entry.getTooltip());
		}
	}
	private void updateHighlightAnimation(int mouseX, int mouseY, float delta) {
		var hoveredEntry = this.getEntryAtPosition(mouseX, mouseY);
		if (hoveredEntry != null
			&& !(hoveredEntry instanceof CategoryTitleConfigEntry)
			&& !(hoveredEntry instanceof PrefixTextConfigEntry)) {
			var index = this.children().indexOf(hoveredEntry);
			if (index >= 0) {
				var entryTop = this.getRowTop(index);
				this.highlightTargetY = entryTop;
				this.highlightHeight = this.itemHeight;
				// Fade in
				this.highlightAlpha = Mth.lerp(ANIMATION_SPEED * delta, this.highlightAlpha, 1.0f);
				if (this.highlightAlpha > 0.95f) this.highlightAlpha = 1.0f;
				// Initialize position if first hover
				if (this.highlightY < 0 || this.lastHoveredEntry == null) this.highlightY = entryTop;
			}
			this.lastHoveredEntry = hoveredEntry;
		} else {
			// Fade out
			this.highlightAlpha = Mth.lerp(ANIMATION_SPEED * delta, this.highlightAlpha, 0.0f);
			if (this.highlightAlpha < 0.05f) {
				this.highlightAlpha = 0;
				this.lastHoveredEntry = null;
				this.highlightY = -1;
			}
		}
		// Smooth position transition with snap to target
		if (this.highlightTargetY >= 0 && this.highlightY >= 0) {
			this.highlightY = Mth.lerp(ANIMATION_SPEED * delta * 2, this.highlightY, this.highlightTargetY);
			// Snap to target when close enough
			if (Math.abs(this.highlightY - this.highlightTargetY) < 1.0f) this.highlightY = this.highlightTargetY;
		}
	}
	private void renderHighlight(@NotNull GuiGraphics guiGraphics) {
		if (this.highlightAlpha <= 0.01f || this.highlightY < 0) return;
		var alpha = (int) (this.highlightAlpha * 48); // Max alpha 48 (0x30)
		var color = alpha << 24 | 0xFFFFFF;
		var left = this.getX();
		var right = this.getX() + this.getWidth();
		var offset = -2; // Move highlight up
		var top = (int) this.highlightY + offset;
		var bottom = top + this.highlightHeight;
		// Clip to visible area
		var visibleTop = this.getY();
		var visibleBottom = this.getY() + this.getHeight();
		if (top < visibleTop) top = visibleTop;
		if (bottom > visibleBottom) bottom = visibleBottom;
		if (top < bottom) guiGraphics.fill(left, top, right, bottom, color);
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// First check if expanded dropdown should handle the click
		if (this.expandedDropdown != null) {
			if (this.expandedDropdown.isMouseOverDropdown(mouseX, mouseY)) return this.expandedDropdown.handleDropdownClick(mouseX,
				mouseY);
			// Click outside dropdown closes it
			this.expandedDropdown.closeDropdown();
			this.expandedDropdown = null;
			// Don't process further if we just closed a dropdown
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (this.expandedDropdown != null) this.expandedDropdown.mouseReleased(mouseX, mouseY, button);
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (this.expandedDropdown != null) if (this.expandedDropdown.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		// First check if expanded dropdown should handle the scroll
		if (this.expandedDropdown != null && this.expandedDropdown.handleDropdownScroll(mouseX, mouseY, vertical)) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
	}
	@Override
	public int getRowWidth() {
		return this.width - 80;
	}
	public void refreshEntries() {
		this.children().forEach(ConfigEntry::refresh);
	}
	public boolean hasEntryError() {
		return this.children().stream().anyMatch(ConfigEntry::hasError);
	}
	@Override
	protected void renderListSeparators(@NotNull GuiGraphics guiGraphics) {
		// don't render separators
	}
}
