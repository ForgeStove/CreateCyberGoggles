package io.github.forgestove.create_cyber_goggles.config.client.gui;
import io.github.forgestove.create_cyber_goggles.config.client.gui.entry.ConfigEntry;
import io.github.forgestove.create_cyber_goggles.config.client.gui.util.SmoothScroll;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
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
	private final Button dropdownButton;
	private final SmoothScroll smoothScrool;
	private int maxVisibleOptions;
	private double smoothScrollOffset;
	private boolean draggingScrollbar;
	public EnumDropdownScreen(
		Enum<?>[] values,
		Supplier<Enum<?>> selectedSupplier,
		Consumer<Enum<?>> onSelect,
		Function<Enum<?>, Component> displayMapper,
		Screen parentScreen,
		Button dropdownButton
	) {
		super(Component.empty());
		this.values = values;
		this.selectedSupplier = selectedSupplier;
		this.onSelect = onSelect;
		this.displayMapper = displayMapper;
		this.parentScreen = parentScreen;
		this.dropdownButton = dropdownButton;
		update();
		smoothScrool = new SmoothScroll(value -> smoothScrollOffset = value, () -> smoothScrollOffset, this::getMaxScrollOffset);
	}
	private void update() {
		maxVisibleOptions = Math.min(
			values.length,
			(parentScreen.height - dropdownButton.getY() - dropdownButton.getHeight() - HEIGHT) / (HEIGHT + GAP)
		);
		smoothScrollOffset = Mth.clamp(
			Arrays.asList(values).indexOf(selectedSupplier.get()) - maxVisibleOptions / 2,
			0,
			getMaxScrollOffset()
		);
	}
	private int getMaxScrollOffset() {
		return Math.max(0, values.length - maxVisibleOptions);
	}
	@Override
	public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		smoothScrool.tick(delta);
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, -100);
		// 将父屏幕绘制为背景
		parentScreen.render(gui, 0, 0, delta);
		pose.popPose();
		var dropdownX = dropdownButton.getX();
		var dropdownY = dropdownY();
		var dropdownWidth = dropdownButton.getWidth();
		var dropdownHeight = maxVisibleOptions * (HEIGHT + GAP);
		// 绘制下拉面板（外圈黑底=边框 + 内部灰色填充）
		gui.fill(dropdownX, dropdownY, dropdownX + dropdownWidth, dropdownY + dropdownHeight + GAP, 0xFF000000);
		gui.fill(dropdownX + 1, dropdownY + 1, dropdownX + dropdownWidth - 1, dropdownY + dropdownHeight + GAP - 1, 0xFF2D2D2D);
		// 绘制可见选项（裁剪到面板内部区域，防止滚动时外溢）
		gui.enableScissor(dropdownX + 1, dropdownY + 1, dropdownX + dropdownWidth - 1, dropdownY + dropdownHeight + GAP - 1);
		var itemHeight = HEIGHT + GAP;
		var firstIndex = Math.max(0, (int) Math.floor(smoothScrollOffset));
		var lastIndex = Math.min(values.length - 1, (int) Math.ceil(smoothScrollOffset + maxVisibleOptions));
		for (var optionIndex = firstIndex; optionIndex <= lastIndex; optionIndex++) {
			var y = (int) Math.round(dropdownY + (optionIndex - smoothScrollOffset) * itemHeight);
			if (y + HEIGHT < dropdownY || y > dropdownY + dropdownHeight + GAP) continue;
			var isSelected = values[optionIndex] == selectedSupplier.get();
			var isHovered = mouseX >= dropdownX && mouseX < dropdownX + dropdownWidth && mouseY >= y && mouseY < y + HEIGHT;
			if (isSelected) gui.fill(dropdownX + GAP, y + GAP, dropdownX + dropdownWidth - GAP, y + HEIGHT + GAP, 0xFF3366BB);
			else if (isHovered) gui.fill(dropdownX + GAP, y + GAP, dropdownX + dropdownWidth - GAP, y + HEIGHT + GAP, 0xFF404040);
			var color = isSelected ? 0xFFFFFFFF : isHovered ? 0xFFFFFF00 : 0xFFE0E0E0;
			gui.drawString(font, displayMapper.apply(values[optionIndex]), dropdownX + HEIGHT / 4, y + HEIGHT / 4 + GAP, color, false);
		}
		gui.disableScissor();
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
	public void resize(@NotNull Minecraft minecraft, int width, int height) {
		parentScreen.resize(minecraft, width, height);
		update();
	}
	private int dropdownY() {
		return dropdownButton.getY() + dropdownButton.getHeight();
	}
	private boolean needsScrollbar() {
		return values.length > maxVisibleOptions;
	}
	private Rectangle getScrollbarTrack() {
		return new Rectangle(
			dropdownButton.getX() + dropdownButton.getWidth() - SCROLLBAR_WIDTH - GAP,
			dropdownY() + GAP,
			SCROLLBAR_WIDTH,
			maxVisibleOptions * (HEIGHT + GAP) - GAP
		);
	}
	private Rectangle getScrollbarThumb() {
		var track = getScrollbarTrack();
		var thumbHeight = Math.max(15, track.height * maxVisibleOptions / values.length);
		var maxScroll = getMaxScrollOffset();
		var thumbY = maxScroll > 0 ? track.y + (track.height - thumbHeight) * smoothScrollOffset / maxScroll : track.y;
		return new Rectangle(track.x, (int) thumbY, track.width, thumbHeight);
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (isOutsidePanel(mouseX, mouseY)) {
			getMinecraft().setScreen(parentScreen);
			playClickSound();
			return true;
		}
		if (needsScrollbar() && getScrollbarTrack().contains(mouseX, mouseY)) {
			draggingScrollbar = true;
			return true;
		}
		// 从鼠标位置反算点击的选项索引
		var dropdownY = dropdownY();
		var itemHeight = HEIGHT + GAP;
		var index = (int) Math.floor((mouseY - dropdownY + smoothScrollOffset * itemHeight) / itemHeight);
		if (index >= 0 && index < values.length) {
			var y = (int) Math.round(dropdownY + (index - smoothScrollOffset) * itemHeight);
			var contentWidth = getContentWidth();
			if (mouseY >= y && mouseY < y + HEIGHT && mouseX >= dropdownButton.getX() && mouseX < dropdownButton.getX() + contentWidth) {
				onSelect.accept(values[index]);
				getMinecraft().setScreen(parentScreen);
				playClickSound();
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		draggingScrollbar = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!draggingScrollbar) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		updateScrollFromMouse(mouseY);
		return true;
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (isOutsidePanel(mouseX, mouseY)) return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
		return smoothScrool.onMouseScroll(vertical, 1);
	}
	private void updateScrollFromMouse(double mouseY) {
		var track = getScrollbarTrack();
		var thumbHeight = Math.max(15, track.height * maxVisibleOptions / values.length);
		double scrollRange = track.height - thumbHeight;
		if (scrollRange <= 0) return;
		var relativeY = mouseY - track.y - thumbHeight / 2.0;
		smoothScrollOffset = Mth.clamp((int) Math.round(relativeY / scrollRange * getMaxScrollOffset()), 0, getMaxScrollOffset());
	}
	private boolean isOutsidePanel(double mouseX, double mouseY) {
		var panelX = dropdownButton.getX() - GAP;
		var panelY = dropdownY() - GAP;
		var panelW = dropdownButton.getWidth() + GAP;
		var panelH = maxVisibleOptions * HEIGHT + GAP * 2;
		return !(mouseX >= panelX) || !(mouseX <= panelX + panelW) || !(mouseY >= panelY) || !(mouseY <= panelY + panelH);
	}
	private void playClickSound() {
		getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
	private int getContentWidth() {
		return needsScrollbar() ? dropdownButton.getWidth() - SCROLLBAR_WIDTH - GAP : dropdownButton.getWidth();
	}
}
