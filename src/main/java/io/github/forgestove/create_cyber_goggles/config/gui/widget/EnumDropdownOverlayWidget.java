package io.github.forgestove.create_cyber_goggles.config.gui.widget;
import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.gui.entry.ConfigEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.function.*;
public final class EnumDropdownOverlayWidget extends AbstractWidget {
	public static final int GAP = ConfigEntry.GAP;
	public static final int HEIGHT = ConfigEntry.HEIGHT;
	public static final int WIDTH = ConfigEntry.WIDTH;
	private static final int SCROLLBAR_WIDTH = GAP * 3;
	private final Enum<?>[] values;
	private final Supplier<Enum<?>> selectedValue;
	private final Consumer<Enum<?>> onSelect;
	private final Function<Enum<?>, Component> displayMapper;
	public boolean expanded;
	private int scrollOffset;
	private int maxVisibleOptions;
	private int screenBottom;
	private boolean draggingScrollbar;
	public EnumDropdownOverlayWidget(
		Enum<?>[] values,
		Supplier<Enum<?>> selectedValue,
		Consumer<Enum<?>> onSelect,
		Function<Enum<?>, Component> displayMapper
	) {
		super(0, 0, WIDTH, HEIGHT, Component.empty());
		this.values = values;
		this.selectedValue = selectedValue;
		this.onSelect = onSelect;
		this.displayMapper = displayMapper;
		maxVisibleOptions = values.length;
	}
	public void updateLayout(int x, int y, int screenBottom) {
		setPosition(x, y);
		this.screenBottom = screenBottom;
		if (!expanded) return;
		recalculateVisibleOptions();
		scrollOffset = Mth.clamp(scrollOffset, 0, getMaxScrollOffset());
	}
	public void toggle() {
		expanded = !expanded;
		if (!expanded) return;
		recalculateVisibleOptions();
		var currentIndex = Arrays.asList(values).indexOf(selectedValue.get());
		scrollOffset = Mth.clamp(currentIndex - maxVisibleOptions / 2, 0, getMaxScrollOffset());
	}
	public void close() {
		expanded = false;
		draggingScrollbar = false;
	}
	public void stopDragging() {
		draggingScrollbar = false;
	}
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (!expanded) return false;
		return getDropdownBounds().contains(mouseX, mouseY);
	}
	public boolean handleClick(double mouseX, double mouseY) {
		if (!expanded) return false;
		if (needsScrollbar() && getScrollbarArea().contains(mouseX, mouseY)) {
			draggingScrollbar = true;
			updateScrollFromMouse(mouseY);
			return true;
		}
		for (var i = 0; i < maxVisibleOptions; i++) {
			var optionIndex = i + scrollOffset;
			if (optionIndex >= values.length) break;
			if (!getOptionArea(i).contains(mouseX, mouseY)) continue;
			onSelect.accept(values[optionIndex]);
			return true;
		}
		return false;
	}
	public boolean handleDrag(double mouseY) {
		if (!draggingScrollbar) return false;
		updateScrollFromMouse(mouseY);
		return true;
	}
	public boolean handleScroll(double mouseX, double mouseY, double vertical) {
		if (!isMouseOver(mouseX, mouseY)) return false;
		scrollOffset = Mth.clamp(scrollOffset - (int) vertical, 0, getMaxScrollOffset());
		return true;
	}
	public void renderOverlay(@NotNull GuiGraphics gui, ConfigCategoryTab<?> tab, int mouseX, int mouseY) {
		if (!expanded) return;
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, 100);
		var x = getX();
		var y = getY();
		var dropdownHeight = getDropdownHeight();
		gui.fill(x - GAP, y - GAP, x + WIDTH + GAP, y + dropdownHeight + GAP, 0xFF000000);
		gui.fill(x, y, x + WIDTH, y + dropdownHeight, 0xFF2D2D2D);
		for (var i = 0; i < maxVisibleOptions; i++) {
			var optionIndex = i + scrollOffset;
			if (optionIndex >= values.length) break;
			var area = getOptionArea(i);
			var enumValue = values[optionIndex];
			var isHovered = area.contains(mouseX, mouseY);
			var isSelected = enumValue == selectedValue.get();
			if (isSelected) gui.fill(area.x + GAP, area.y, area.x + area.width - GAP, area.y + area.height, 0xFF3366BB);
			else if (isHovered) gui.fill(area.x + GAP, area.y, area.x + area.width - GAP, area.y + area.height, 0xFF404040);
			var textColor = isSelected ? 0xFFFFFFFF : isHovered ? 0xFFFFFF00 : 0xFFE0E0E0;
			gui.drawString(tab.getMinecraft().font, displayMapper.apply(enumValue), x + 4, area.y + 4, textColor, false);
		}
		if (needsScrollbar()) {
			var scrollbarArea = getScrollbarArea();
			gui.fill(
				scrollbarArea.x,
				scrollbarArea.y,
				scrollbarArea.x + scrollbarArea.width,
				scrollbarArea.y + scrollbarArea.height,
				0xFF1A1A1A
			);
			var thumbArea = getScrollbarThumbArea();
			var thumbHovered = thumbArea.contains(mouseX, mouseY);
			gui.fill(
				thumbArea.x,
				thumbArea.y,
				thumbArea.x + thumbArea.width,
				thumbArea.y + thumbArea.height,
				thumbHovered || draggingScrollbar ? 0xFF888888 : 0xFF555555
			);
		}
		pose.popPose();
	}
	private void recalculateVisibleOptions() {
		var availableHeight = screenBottom - getY() - GAP * 2;
		maxVisibleOptions = Mth.clamp(availableHeight / HEIGHT, 1, values.length);
	}
	private int getMaxScrollOffset() {
		return Math.max(0, values.length - maxVisibleOptions);
	}
	private boolean needsScrollbar() {
		return values.length > maxVisibleOptions;
	}
	private int getDropdownHeight() {
		return maxVisibleOptions * HEIGHT + GAP * 2;
	}
	private int getContentWidth() {
		return needsScrollbar() ? WIDTH - SCROLLBAR_WIDTH - GAP : WIDTH;
	}
	private Rectangle getDropdownBounds() {
		return new Rectangle(getX() - GAP, getY() - GAP, WIDTH + GAP, getDropdownHeight() + GAP);
	}
	private Rectangle getOptionArea(int visibleIndex) {
		return new Rectangle(getX(), getY() + GAP + visibleIndex * HEIGHT, getContentWidth(), HEIGHT - GAP);
	}
	private Rectangle getScrollbarArea() {
		return new Rectangle(getX() + WIDTH - SCROLLBAR_WIDTH - GAP, getY() + GAP, SCROLLBAR_WIDTH, getDropdownHeight() - GAP * 2);
	}
	private Rectangle getScrollbarThumbArea() {
		var track = getScrollbarArea();
		var thumbHeight = Math.max(15, track.height * maxVisibleOptions / values.length);
		var maxScroll = getMaxScrollOffset();
		var thumbY = track.y + (maxScroll > 0 ? (track.height - thumbHeight) * scrollOffset / maxScroll : 0);
		return new Rectangle(track.x, thumbY, track.width, thumbHeight);
	}
	private void updateScrollFromMouse(double mouseY) {
		var track = getScrollbarArea();
		var thumbHeight = Math.max(15, track.height * maxVisibleOptions / values.length);
		double scrollRange = track.height - thumbHeight;
		if (!(scrollRange > 0)) return;
		var relativeY = mouseY - track.y - thumbHeight / 2.0;
		scrollOffset = Mth.clamp((int) Math.round(relativeY / scrollRange * getMaxScrollOffset()), 0, getMaxScrollOffset());
	}
	@Override
	protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int i1, float v) {}
	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}
}
