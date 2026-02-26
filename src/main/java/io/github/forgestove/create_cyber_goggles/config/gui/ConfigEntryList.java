package io.github.forgestove.create_cyber_goggles.config.gui;
import io.github.forgestove.create_cyber_goggles.config.gui.entry.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.*;
public final class ConfigEntryList extends ContainerObjectSelectionList<ConfigEntry> {
	private static final float ANIMATION_SPEED = 0.4f;
	private final ConfigCategoryTab<?> tab;
	private final PanoramaRenderer panoramaRenderer;
	@Nullable private EnumValueConfigEntry<?, ?> expandedDropdown;
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
		@NotNull Iterable<ConfigEntry> entries,
		PanoramaRenderer panoramaRenderer
	) {
		super(minecraft, width, contentHeight, headerHeight, headerHeight + contentHeight, itemSpacing);
		this.tab = tab;
		entries.forEach(this::addEntry);
		this.setRenderBackground(false);
		this.panoramaRenderer = panoramaRenderer;
	}
	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		// 渲染全景图背景（在所有控件之前）
		if (Minecraft.getInstance().level == null) this.panoramaRenderer.render(delta, 1.0F);
		// Update highlight animation first
		this.updateHighlightAnimation(mouseX, mouseY, delta);
		// Render list entries (this also renders background)
		super.render(guiGraphics, mouseX, mouseY, delta);
		// Track which dropdown is expanded
		this.expandedDropdown = null;
		for (var entry : this.children())
			if (entry instanceof EnumValueConfigEntry<?, ?> enumEntry && enumEntry.isExpanded()) {
				this.expandedDropdown = enumEntry;
				break;
			}
		// Render dropdown overlay on top of everything (outside scissor)
		var showTooltip = this.expandedDropdown == null;
		if (this.expandedDropdown != null) {
			this.expandedDropdown.renderDropdownOverlay(guiGraphics, mouseX, mouseY);
			// Don't show tooltips when mouse is over the dropdown
			showTooltip = !this.expandedDropdown.isMouseOverDropdown(mouseX, mouseY);
		}
		if (showTooltip) {
			this.renderHighlight(guiGraphics);
			this.updateHighlightAnimation(mouseX, mouseY, delta);
		}
		// Render highlight before entries
		super.renderList(guiGraphics, mouseX, mouseY, delta);
		// Tooltips
		if (!showTooltip) return;
		var entry = this.getEntryAt(mouseX, mouseY);
		if (entry == null) return;
		if (entry instanceof ValueConfigEntry<?, ?, ?> valueEntry) if (valueEntry.resetButton.isHovered()) {
			this.tab.getScreen().setTooltipForNextRenderPass(Tooltip.splitTooltip(this.minecraft, Translation.RESET_TOOLTIP));
			return;
		} else if (valueEntry.undoButton.isHovered()) {
			this.tab.getScreen().setTooltipForNextRenderPass(Tooltip.splitTooltip(this.minecraft, Translation.UNDO_TOOLTIP));
			return;
		} else if (valueEntry instanceof ColorValueConfigEntry<?> colorEntry && colorEntry.pickerButton.isHovered()) {
			this.tab.getScreen().setTooltipForNextRenderPass(Tooltip.splitTooltip(this.minecraft, Translation.COLOR_PICKER_TOOLTIP));
			return;
		}
		if (entry.getTooltip() != null) this.tab.getScreen().setTooltipForNextRenderPass(entry.getTooltip());
	}
	private void updateHighlightAnimation(int mouseX, int mouseY, float delta) {
		var hoveredEntry = this.getEntryAt(mouseX, mouseY);
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
		// Use 0 and width for full width highlight
		var left = 0;
		var right = this.width;
		var offset = -1; // Move highlight up
		var top = (int) this.highlightY + offset;
		var bottom = top + this.highlightHeight;
		// Clip to visible area
		var visibleTop = this.y0;
		var visibleBottom = this.y1;
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
		// Find and click the entry at this position
		var entry = this.getEntryAt(mouseX, mouseY);
		if (entry != null && entry.mouseClicked(mouseX, mouseY, button)) {
			this.setFocused(entry);
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
	public boolean mouseScrolled(double mouseX, double mouseY, double vertical) {
		// First check if expanded dropdown should handle the scroll
		if (this.expandedDropdown != null && this.expandedDropdown.handleDropdownScroll(mouseX, mouseY, vertical)) return true;
		return super.mouseScrolled(mouseX, mouseY, vertical);
	}
	@Override
	public int getRowWidth() {
		return this.width - 80;
	}
	@Override
	protected void renderList(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		// Render highlight first (behind entries) while inside scissor region
		this.renderHighlight(guiGraphics);
		// Then render entries on top
		super.renderList(guiGraphics, mouseX, mouseY, delta);
	}
	/**
	 * Custom method to get entry at position using full width instead of limited row width
	 */
	@Nullable
	public ConfigEntry getEntryAt(double mouseX, double mouseY) {
		// Use full width (x0 to x1) instead of row width for hit detection
		if (mouseX < this.x0 || mouseX > this.x1 || mouseY < this.y0 || mouseY > this.y1) return null;
		var scrolledY = (int) (mouseY - this.y0 + this.getScrollAmount() - 4);
		var index = scrolledY / this.itemHeight;
		if (index >= 0 && index < this.getItemCount()) return this.getEntry(index);
		return null;
	}
	public void refreshEntries() {
		this.children().forEach(ConfigEntry::refresh);
	}
	public boolean hasEntryError() {
		return this.children().stream().anyMatch(ConfigEntry::hasError);
	}
	@Override
	protected int getScrollbarPosition() {
		return this.x1 - 36;
	}
}
