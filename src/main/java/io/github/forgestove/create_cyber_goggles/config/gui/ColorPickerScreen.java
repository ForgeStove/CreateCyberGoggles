package io.github.forgestove.create_cyber_goggles.config.gui;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.forgestove.create_cyber_goggles.config.Translation;
import io.github.forgestove.create_cyber_goggles.config.gui.entry.ConfigEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.*;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;
public final class ColorPickerScreen extends Screen {
	/** 颜色选择区域的边长 */
	private static final int PICKER_SIZE = 128;
	/** 色相/透明度滑条宽度 */
	private static final int BAR_WIDTH = 16;
	/** 面板内边距 */
	private static final int PADDING = ConfigEntry.GAP * 4;
	/** 基础控件尺寸 */
	private static final int SIZE = ConfigEntry.SIZE;
	/** 确认/取消按钮宽度 */
	private static final int BUTTON_WIDTH = SIZE * 2;
	private static final Pattern HEX_PATTERN = Pattern.compile("[0-9A-Fa-f]*");
	private final Screen parent;
	private final boolean hasAlpha;
	/** 颜色确认后的回调 */
	private final IntConsumer onColorSelected;
	/** 面板左上角坐标 */
	private final Point pickerPos = new Point();
	/** 饱和度/亮度选择区坐标 */
	private final Point svPos = new Point();
	/** 色相 */
	private float hue;
	/** 饱和度 */
	private float saturation = 1f;
	/** 亮度 */
	private float brightness = 1f;
	/** 透明度 */
	private int alpha = 255;
	/** 当前拖拽模式 */
	private DragMode dragMode = DragMode.NONE;
	/** 饱和度/亮度方块缓存 */
	private int[] sbPixels;
	/** 缓存时对应的色相值 */
	private float cachedHue = -1f;
	/** 十六进制颜色输入框 */
	private ConfigEditBox hexInput;
	/** 防止输入框与颜色状态互相递归更新 */
	private boolean updatingFromInput;
	public ColorPickerScreen(Screen parent, int initialColor, boolean hasAlpha, IntConsumer onColorSelected) {
		super(Translation.COLOR_PICKER_TOOLTIP);
		this.parent = parent;
		this.hasAlpha = hasAlpha;
		this.onColorSelected = onColorSelected;
		updateHSBFromColor(initialColor);
	}
	private Rectangle getSBArea() {
		return new Rectangle(svPos.x, svPos.y, PICKER_SIZE, PICKER_SIZE);
	}
	private Rectangle getHueBarArea() {
		return new Rectangle(getHueBarX(), svPos.y, BAR_WIDTH, PICKER_SIZE);
	}
	private Rectangle getAlphaBarArea() {
		return new Rectangle(getAlphaBarX(), svPos.y, BAR_WIDTH, PICKER_SIZE);
	}
	@Override
	protected void init() {
		layoutPicker();
		var buttonY = svPos.y + PICKER_SIZE + PADDING;
		var okButton = Button.builder(
			Component.translatable("gui.ok"), _ -> {
				onColorSelected.accept(colorFromHSB());
				minecraft.gui.setScreen(parent);
			}
		).bounds(svPos.x, buttonY, BUTTON_WIDTH, SIZE).build();
		var cancelButton = Button.builder(Component.translatable("gui.cancel"), _ -> minecraft.gui.setScreen(parent))
			.bounds(svPos.x + BUTTON_WIDTH + 2, buttonY, BUTTON_WIDTH, SIZE)
			.build();
		hexInput = createHexInput(buttonY);
		var previewWidget = new ColorPreviewWidget(
			hexInput.getX() + hexInput.getWidth() + 4,
			buttonY,
			SIZE,
			SIZE,
			hasAlpha,
			this::colorFromHSB
		);
		addRenderableWidget(okButton);
		addRenderableWidget(cancelButton);
		addRenderableWidget(hexInput);
		addRenderableWidget(previewWidget);
	}
	private void layoutPicker() {
		var totalWidth = PICKER_SIZE + PADDING + BAR_WIDTH + (hasAlpha ? PADDING + BAR_WIDTH : 0);
		var totalHeight = PICKER_SIZE + PADDING + 24;
		pickerPos.move((width - totalWidth) / 2 - PADDING, (height - totalHeight) / 2 - PADDING - 10);
		svPos.move(pickerPos.x + PADDING, pickerPos.y + PADDING + 16);
	}
	private ConfigEditBox createHexInput(int buttonY) {
		var hexInputX = svPos.x + BUTTON_WIDTH * 2 + 6;
		var hexInputWidth = hasAlpha ? 70 : 55;
		var input = new ConfigEditBox(font, hexInputX, buttonY, hexInputWidth, SIZE, Component.literal("Hex"));
		input.setFilter(text -> HEX_PATTERN.matcher(text).matches());
		input.setMaxLength(hasAlpha ? 8 : 6);
		input.setValue(formatHexColor());
		input.setResponder(this::onHexInputChange);
		return input;
	}
	private void updateHSBFromColor(int color) {
		if (hasAlpha) alpha = ARGB.alpha(color);
		var hsb = Color.RGBtoHSB(ARGB.red(color), ARGB.green(color), ARGB.blue(color), null);
		hue = hsb[0];
		saturation = hsb[1];
		brightness = hsb[2];
	}
	private int colorFromHSB() {
		var rgb = Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
		return hasAlpha ? alpha << 24 | rgb : rgb;
	}
	private String formatHexColor() {
		var color = colorFromHSB();
		return hasAlpha ? String.format("%08X", color) : String.format("%06X", color & 0xFFFFFF);
	}
	private void onHexInputChange(String value) {
		if (updatingFromInput || value.isEmpty()) return;
		try {
			var color = Integer.parseUnsignedInt(value, 16);
			updatingFromInput = true;
			updateHSBFromColor(hasAlpha ? color : 0xFF000000 | color);
		} catch (NumberFormatException ignored) {
		}
	}
	private void updateHexInput() {
		if (hexInput == null || updatingFromInput) return;
		updatingFromInput = true;
		hexInput.setValue(formatHexColor());
		updatingFromInput = false;
	}
	@Override
	public void extractBackground(@NonNull GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
		if (parent != null) parent.extractRenderState(gui, 0, 0, delta);
		super.extractBackground(gui, mouseX, mouseY, delta);
	}
	@Override
	public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
		var totalWidth = PICKER_SIZE + PADDING + BAR_WIDTH + (hasAlpha ? PADDING + BAR_WIDTH : 0) + PADDING * 2 + 8;
		var totalHeight = PICKER_SIZE + PADDING * 2 + 16 + 28;
		gui.fill(pickerPos.x - 4, pickerPos.y - 4, pickerPos.x + totalWidth + 4, pickerPos.y + totalHeight + 4, 0xF0101010);
		gui.outline(pickerPos.x - 4, pickerPos.y - 4, totalWidth + 8, totalHeight + 8, 0xFF404040);
		gui.text(font, title, svPos.x, pickerPos.y + PADDING, 0xFFFFFFFF);
		if (cachedHue != hue) rebuildSBCache();
		renderSBSquare(gui, svPos.x, svPos.y);
		var hueBarX = getHueBarX();
		renderHueBar(gui, hueBarX, svPos.y);
		if (hasAlpha) renderAlphaBar(gui, getAlphaBarX(), svPos.y);
		renderSelectors(gui, hueBarX);
		// Keep widget rendering order controlled in this screen.
		for (var child : children()) if (child instanceof AbstractWidget widget) widget.extractRenderState(gui, mouseX, mouseY, delta);
	}
	private void rebuildSBCache() {
		if (sbPixels == null) sbPixels = new int[PICKER_SIZE * PICKER_SIZE];
		for (var y = 0; y < PICKER_SIZE; y++) {
			var b = 1f - (float) y / (PICKER_SIZE - 1);
			for (var x = 0; x < PICKER_SIZE; x++) {
				var s = (float) x / (PICKER_SIZE - 1);
				sbPixels[y * PICKER_SIZE + x] = 0xFF000000 | Color.HSBtoRGB(hue, s, b) & 0xFFFFFF;
			}
		}
		cachedHue = hue;
	}
	private void renderSelectors(GuiGraphicsExtractor gui, int hueBarX) {
		var sbSelectorX = svPos.x + (int) (saturation * (PICKER_SIZE - 1));
		var sbSelectorY = svPos.y + (int) ((1f - brightness) * (PICKER_SIZE - 1));
		gui.outline(sbSelectorX - 4, sbSelectorY - 4, 9, 9, 0xFFFFFFFF);
		gui.outline(sbSelectorX - 3, sbSelectorY - 3, 7, 7, 0xFF000000);
		var hueSelectorY = svPos.y + (int) (hue * (PICKER_SIZE - 1));
		renderHorizontalArrow(gui, hueBarX, hueSelectorY);
		if (!hasAlpha) return;
		var alphaSelectorY = svPos.y + (int) ((1f - alpha / 255f) * (PICKER_SIZE - 1));
		renderHorizontalArrow(gui, getAlphaBarX(), alphaSelectorY);
	}
	private int getHueBarX() {
		return svPos.x + PICKER_SIZE + PADDING;
	}
	private int getAlphaBarX() {
		return getHueBarX() + BAR_WIDTH + PADDING;
	}
	private void renderHorizontalArrow(GuiGraphicsExtractor gui, int x, int y) {
		gui.fill(x - 3, y - 1, x, y + 2, 0xFFFFFFFF);
		gui.fill(x - 2, y, x, y + 1, 0xFF000000);
		gui.fill(x + BAR_WIDTH, y - 1, x + BAR_WIDTH + 3, y + 2, 0xFFFFFFFF);
		gui.fill(x + BAR_WIDTH, y, x + BAR_WIDTH + 2, y + 1, 0xFF000000);
	}
	private void renderSBSquare(GuiGraphicsExtractor gui, int x, int y) {
		var blockSize = 4;
		for (var by = 0; by < PICKER_SIZE; by += blockSize)
			for (var bx = 0; bx < PICKER_SIZE; bx += blockSize) {
				var color = sbPixels[by * PICKER_SIZE + bx];
				var endX = Math.min(bx + blockSize, PICKER_SIZE);
				var endY = Math.min(by + blockSize, PICKER_SIZE);
				gui.fill(x + bx, y + by, x + endX, y + endY, color);
			}
		gui.outline(x, y, PICKER_SIZE, PICKER_SIZE, 0xFF000000);
	}
	private void renderHueBar(GuiGraphicsExtractor gui, int x, int y) {
		var step = 4;
		for (var dy = 0; dy < PICKER_SIZE; dy += step) {
			var h = (float) dy / (PICKER_SIZE - 1);
			var color = 0xFF000000 | Color.HSBtoRGB(h, 1f, 1f) & 0xFFFFFF;
			gui.fill(x, y + dy, x + BAR_WIDTH, y + Math.min(dy + step, PICKER_SIZE), color);
		}
		gui.outline(x, y, BAR_WIDTH, PICKER_SIZE, 0xFF000000);
	}
	private void renderAlphaBar(GuiGraphicsExtractor gui, int x, int y) {
		var baseColor = colorFromHSB() & 0xFFFFFF;
		var step = 4;
		for (var dy = 0; dy < PICKER_SIZE; dy += step) {
			var a = 255 - dy * 255 / (PICKER_SIZE - 1);
			gui.fill(x, y + dy, x + BAR_WIDTH, y + Math.min(dy + step, PICKER_SIZE), a << 24 | baseColor);
		}
		gui.outline(x, y, BAR_WIDTH, PICKER_SIZE, 0xFF000000);
	}
	@Override
	public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean bl) {
		var button = event.button();
		var mouseX = event.x();
		var mouseY = event.y();
		ConfigEditBox.clearFocusIfNotHovered(mouseX, mouseY);
		if (button != 0) return super.mouseClicked(event, false);
		if (getSBArea().contains(mouseX, mouseY)) {
			setFocused(null);
			dragMode = DragMode.SV;
			updateFromMouse((int) mouseX, (int) mouseY);
			return true;
		}
		var hueBarArea = getHueBarArea();
		if (hueBarArea.contains(mouseX, mouseY)) {
			setFocused(null);
			dragMode = DragMode.HUE;
			updateFromMouse((int) mouseX, (int) mouseY);
			return true;
		}
		if (hasAlpha && getAlphaBarArea().contains(mouseX, mouseY)) {
			setFocused(null);
			dragMode = DragMode.ALPHA;
			updateFromMouse((int) mouseX, (int) mouseY);
			return true;
		}
		var handled = super.mouseClicked(event, false);
		if (!handled) setFocused(null);
		return handled;
	}
	@Override
	public boolean mouseDragged(@NotNull MouseButtonEvent event, double deltaX, double deltaY) {
		if (event.button() == 0 && dragMode != DragMode.NONE) {
			updateFromMouse((int) event.x(), (int) event.y());
			return true;
		}
		return super.mouseDragged(event, deltaX, deltaY);
	}
	@Override
	public boolean mouseReleased(@NotNull MouseButtonEvent event) {
		if (event.button() == 0) dragMode = DragMode.NONE;
		return super.mouseReleased(event);
	}
	private void updateFromMouse(int mouseX, int mouseY) {
		if (dragMode == DragMode.SV) {
			saturation = Mth.clamp((float) (mouseX - svPos.x) / (PICKER_SIZE - 1), 0f, 1f);
			brightness = Mth.clamp(1f - (float) (mouseY - svPos.y) / (PICKER_SIZE - 1), 0f, 1f);
		} else if (dragMode == DragMode.HUE) hue = Mth.clamp((float) (mouseY - svPos.y) / (PICKER_SIZE - 1), 0f, 1f);
		else if (dragMode == DragMode.ALPHA) alpha = 255 - Mth.clamp((mouseY - svPos.y) * 255 / (PICKER_SIZE - 1), 0, 255);
		updateHexInput();
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (getHueBarArea().contains(mouseX, mouseY)) {
			var hueStep = 0.005f;
			hue = Mth.clamp(hue - (float) scrollY * hueStep, 0f, 1f);
			updateHexInput();
			return true;
		}
		if (hasAlpha && getAlphaBarArea().contains(mouseX, mouseY)) {
			var alphaStep = 1;
			alpha = Mth.clamp(alpha + (int) (scrollY * alphaStep), 0, 255);
			updateHexInput();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}
	@Override
	public boolean keyPressed(@NotNull KeyEvent event) {
		var modifiers = event.modifiers();
		var hasShift = (modifiers & 1) != 0;
		var step = hasShift ? 0.05f : 0.01f;
		var intStep = hasShift ? 16 : 1;
		var window = minecraft.getWindow();
		var up = InputConstants.isKeyDown(window, InputConstants.KEY_UP);
		var down = InputConstants.isKeyDown(window, InputConstants.KEY_DOWN);
		var left = InputConstants.isKeyDown(window, InputConstants.KEY_LEFT);
		var right = InputConstants.isKeyDown(window, InputConstants.KEY_RIGHT);
		var pageUp = InputConstants.isKeyDown(window, InputConstants.KEY_PAGEUP);
		var pageDown = InputConstants.isKeyDown(window, InputConstants.KEY_PAGEDOWN);
		var home = hasAlpha && InputConstants.isKeyDown(window, InputConstants.KEY_HOME);
		var end = hasAlpha && InputConstants.isKeyDown(window, InputConstants.KEY_END);
		var handled = up || down || left || right || pageUp || pageDown || home || end;
		if (hexInput != null && hexInput.isFocused() && handled) return super.keyPressed(event);
		if (!handled) return super.keyPressed(event);
		if (up) brightness = Mth.clamp(brightness + step, 0f, 1f);
		if (down) brightness = Mth.clamp(brightness - step, 0f, 1f);
		if (left) saturation = Mth.clamp(saturation - step, 0f, 1f);
		if (right) saturation = Mth.clamp(saturation + step, 0f, 1f);
		if (pageUp) hue = Mth.clamp(hue - step, 0f, 1f);
		if (pageDown) hue = Mth.clamp(hue + step, 0f, 1f);
		if (home) alpha = Mth.clamp(alpha + intStep, 0, 255);
		if (end) alpha = Mth.clamp(alpha - intStep, 0, 255);
		updateHexInput();
		return true;
	}
	@Override
	public void resize(int width, int height) {
		if (parent != null) parent.resize(width, height);
		super.resize(width, height);
	}
	public enum DragMode {
		/** 未拖拽 */
		NONE,
		/** 饱和度 / 亮度区域 */
		SV,
		/** 色相滑条 */
		HUE,
		/** 透明度滑条 */
		ALPHA
	}
}
