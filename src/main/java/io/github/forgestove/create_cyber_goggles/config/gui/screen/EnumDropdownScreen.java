package io.github.forgestove.create_cyber_goggles.config.gui.screen;
import io.github.forgestove.create_cyber_goggles.config.gui.entry.ConfigEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.function.*;
public final class EnumDropdownScreen extends Screen {
	private static final int GAP = ConfigEntry.GAP, HEIGHT = ConfigEntry.HEIGHT, SCROLLBAR_WIDTH = HEIGHT / 4;
	private final Enum<?>[] values;
	private final Supplier<Enum<?>> selectedSupplier;
	private final Consumer<Enum<?>> onSelect;
	private final Function<Enum<?>, Component> displayMapper;
	private final Screen parentScreen;
	private final int dropdownX, dropdownY, dropdownWidth, maxVisibleOptions;
	private int scrollOffset;
	private boolean draggingScrollbar;
	public EnumDropdownScreen(
		Enum<?>[] values,
		Supplier<Enum<?>> selectedSupplier,
		Consumer<Enum<?>> onSelect,
		Function<Enum<?>, Component> displayMapper,
		Screen parentScreen,
		int dropdownX,
		int dropdownY,
		int dropdownWidth,
		int screenHeight
	) {
		super(Component.empty());
		this.values = values;
		this.selectedSupplier = selectedSupplier;
		this.onSelect = onSelect;
		this.displayMapper = displayMapper;
		this.parentScreen = parentScreen;
		this.dropdownX = dropdownX;
		this.dropdownY = dropdownY;
		this.dropdownWidth = dropdownWidth;
		maxVisibleOptions = Math.min(values.length, (screenHeight - dropdownY - HEIGHT) / (HEIGHT + GAP));
		scrollOffset = Mth.clamp(Arrays.asList(values).indexOf(selectedSupplier.get()) - maxVisibleOptions / 2, 0, getMaxScrollOffset());
		width = parentScreen.width;
		height = parentScreen.height;
	}
	@Override
	public void resize(@NotNull Minecraft minecraft, int width, int height) {
		// 窗口大小改变后，下拉面板坐标失效，直接关闭
		minecraft.setScreen(parentScreen);
	}
	@Override
	public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, -100);
		// 将父屏幕绘制为背景（无默认菜单背景，无模糊）
		parentScreen.render(gui, mouseX, mouseY, delta);
		pose.popPose();
		// 绘制下拉面板
		var dropdownHeight = maxVisibleOptions * (HEIGHT + GAP);
		gui.fill(dropdownX, dropdownY, dropdownX + dropdownWidth, dropdownY + dropdownHeight + GAP, 0xFF2D2D2D);
		// 绘制可见选项
		for (var i = 0; i < maxVisibleOptions; i++) {
			var optionIndex = i + scrollOffset;
			if (optionIndex >= values.length) break;
			var area = getOptionArea(i);
			var isSelected = values[optionIndex] == selectedSupplier.get();
			var isHovered = area.contains(mouseX, mouseY);
			if (isSelected) gui.fill(area.x + GAP, area.y + GAP, area.x + area.width - GAP, area.y + area.height + GAP, 0xFF3366BB);
			else if (isHovered) gui.fill(area.x + GAP, area.y + GAP, area.x + area.width - GAP, area.y + area.height + GAP, 0xFF404040);
			var color = isSelected ? 0xFFFFFFFF : isHovered ? 0xFFFFFF00 : 0xFFE0E0E0;
			gui.drawString(font, displayMapper.apply(values[optionIndex]), dropdownX + HEIGHT / 4, area.y + HEIGHT / 4 + GAP, color,
				false);
		}
		// 如果需要则绘制滚动条
		if (!needsScrollbar()) return;
		var track = getScrollbarTrack();
		gui.fill(track.x, track.y, track.x + track.width, track.y + track.height, 0xFF1A1A1A);
		var thumb = getScrollbarThumb();
		var thumbHovered = thumb.contains(mouseX, mouseY);
		gui.fill(
			thumb.x,
			thumb.y,
			thumb.x + thumb.width,
			thumb.y + thumb.height,
			thumbHovered || draggingScrollbar ? 0xFF888888 : 0xFF555555
		);
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!isInsidePanel(mouseX, mouseY)) {
			// 点击外部区域 → 关闭
			getMinecraft().setScreen(parentScreen);
			return true;
		}
		if (needsScrollbar() && getScrollbarTrack().contains(mouseX, mouseY)) {
			draggingScrollbar = true;
			updateScrollFromMouse(mouseY);
			return true;
		}
		for (var i = 0; i < maxVisibleOptions; i++) {
			var optionIndex = i + scrollOffset;
			if (optionIndex >= values.length) break;
			if (!getOptionArea(i).contains(mouseX, mouseY)) continue;
			onSelect.accept(values[optionIndex]);
			getMinecraft().setScreen(parentScreen); // 选择后关闭
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!draggingScrollbar) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		updateScrollFromMouse(mouseY);
		return true;
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		draggingScrollbar = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (!isInsidePanel(mouseX, mouseY)) return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
		scrollOffset = Mth.clamp(scrollOffset - (int) vertical, 0, getMaxScrollOffset());
		return true;
	}
	private boolean isInsidePanel(double mouseX, double mouseY) {
		var panelX = dropdownX - GAP;
		var panelY = dropdownY - GAP;
		var panelW = dropdownWidth + GAP;
		var panelH = maxVisibleOptions * HEIGHT + GAP * 2;
		return mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= panelY && mouseY <= panelY + panelH;
	}
	private int getMaxScrollOffset() {
		return Math.max(0, values.length - maxVisibleOptions);
	}
	private boolean needsScrollbar() {
		return values.length > maxVisibleOptions;
	}
	private int getContentWidth() {
		return needsScrollbar() ? dropdownWidth - SCROLLBAR_WIDTH - GAP : dropdownWidth;
	}
	private Rectangle getOptionArea(int visibleIndex) {
		return new Rectangle(dropdownX, dropdownY + visibleIndex * (HEIGHT + GAP), getContentWidth(), HEIGHT);
	}
	private Rectangle getScrollbarTrack() {
		return new Rectangle(
			dropdownX + dropdownWidth - SCROLLBAR_WIDTH - GAP,
			dropdownY + GAP,
			SCROLLBAR_WIDTH,
			maxVisibleOptions * HEIGHT
		);
	}
	private Rectangle getScrollbarThumb() {
		var track = getScrollbarTrack();
		var thumbHeight = Math.max(15, track.height * maxVisibleOptions / values.length);
		var maxScroll = getMaxScrollOffset();
		var thumbY = maxScroll > 0 ? track.y + (track.height - thumbHeight) * scrollOffset / maxScroll : track.y;
		return new Rectangle(track.x, thumbY, track.width, thumbHeight);
	}
	private void updateScrollFromMouse(double mouseY) {
		var track = getScrollbarTrack();
		var thumbHeight = Math.max(15, track.height * maxVisibleOptions / values.length);
		double scrollRange = track.height - thumbHeight;
		if (scrollRange <= 0) return;
		var relativeY = mouseY - track.y - thumbHeight / 2.0;
		scrollOffset = Mth.clamp((int) Math.round(relativeY / scrollRange * getMaxScrollOffset()), 0, getMaxScrollOffset());
	}
}