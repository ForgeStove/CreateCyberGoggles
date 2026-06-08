package io.github.forgestove.create_cyber_goggles.config.gui.screen;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.forgestove.create_cyber_goggles.config.Translation;
import io.github.forgestove.create_cyber_goggles.config.gui.entry.ConfigEntry;
import io.github.forgestove.create_cyber_goggles.config.gui.widget.ColorPreviewWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;
public final class ColorPickerScreen extends Screen {
	private static final int PICKER_SIZE = 128;
	private static final int BAR_WIDTH = 16;
	private static final int PADDING = ConfigEntry.GAP * 4;
	private static final int SIZE = ConfigEntry.SIZE;
	private static final int BUTTON_WIDTH = SIZE * 2;
	private static final Pattern HEX_PATTERN = Pattern.compile("[0-9A-Fa-f]*");
	private final Screen parent;
	private final boolean hasAlpha;
	private final IntConsumer onColorSelected;
	private float hue;
	private float saturation = 1f;
	private float brightness = 1f;
	private int alpha = 255;
	private boolean draggingSV;
	private boolean draggingHue;
	private boolean draggingAlpha;
	private int pickerX;
	private int pickerY;
	private int svX;
	private int svY;
	private int[] sbPixels;
	private float cachedHue = -1f;
	private EditBox hexInput;
	private boolean updatingFromInput;
	public ColorPickerScreen(Screen parent, int initialColor, boolean hasAlpha, IntConsumer onColorSelected) {
		super(Translation.COLOR_PICKER_TOOLTIP);
		this.parent = parent;
		this.hasAlpha = hasAlpha;
		this.onColorSelected = onColorSelected;
		updateHSBFromColor(initialColor);
	}
	private Rectangle getSBArea() {
		return new Rectangle(svX, svY, PICKER_SIZE, PICKER_SIZE);
	}
	private Rectangle getHueBarArea() {
		return new Rectangle(getHueBarX(), svY, BAR_WIDTH, PICKER_SIZE);
	}
	private Rectangle getAlphaBarArea() {
		return new Rectangle(getAlphaBarX(), svY, BAR_WIDTH, PICKER_SIZE);
	}
	@Override
	protected void init() {
		layoutPicker();
		if (minecraft == null) return;
		var buttonY = svY + PICKER_SIZE + PADDING;
		addRenderableWidget(Button.builder(
			Component.translatable("gui.ok"), b -> {
				onColorSelected.accept(colorFromHSB());
				minecraft.setScreen(parent);
			}
		).bounds(svX, buttonY, BUTTON_WIDTH, SIZE).build());
		addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> minecraft.setScreen(parent))
			.bounds(svX + BUTTON_WIDTH + 2, buttonY, BUTTON_WIDTH, SIZE)
			.build());
		hexInput = createHexInput(buttonY);
		addRenderableWidget(hexInput);
		var previewWidget = new ColorPreviewWidget(
			hexInput.getX() + hexInput.getWidth() + 4,
			buttonY,
			SIZE,
			SIZE,
			hasAlpha,
			this::colorFromHSB
		);
		addRenderableWidget(previewWidget);
	}
	private void layoutPicker() {
		var totalWidth = PICKER_SIZE + PADDING + BAR_WIDTH + (hasAlpha ? PADDING + BAR_WIDTH : 0);
		var totalHeight = PICKER_SIZE + PADDING + 24;
		pickerX = (width - totalWidth) / 2 - PADDING;
		pickerY = (height - totalHeight) / 2 - PADDING - 10;
		svX = pickerX + PADDING;
		svY = pickerY + PADDING + 16;
	}
	private EditBox createHexInput(int buttonY) {
		var hexInputX = svX + BUTTON_WIDTH * 2 + 6;
		var hexInputWidth = hasAlpha ? 70 : 55;
		var input = new EditBox(font, hexInputX, buttonY, hexInputWidth, SIZE, Component.literal("Hex"));
		input.setMaxLength(hasAlpha ? 8 : 6);
		input.setValue(formatHexColor());
		input.setFilter(s -> HEX_PATTERN.matcher(s).matches());
		input.setResponder(this::onHexInputChange);
		return input;
	}
	private void updateHSBFromColor(int color) {
		if (hasAlpha) alpha = ARGB32.alpha(color);
		var hsb = Color.RGBtoHSB(ARGB32.red(color), ARGB32.green(color), ARGB32.blue(color), null);
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
		if (updatingFromInput) return;
		try {
			var color = (int) Long.parseLong(value, 16);
			updatingFromInput = true;
			updateHSBFromColor(hasAlpha ? color : 0xFF000000 | color);
			updatingFromInput = false;
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
	public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		if (parent != null) parent.render(gui, -1, -1, delta);
		renderBlurredBackground(delta);
		var totalWidth = PICKER_SIZE + PADDING + BAR_WIDTH + (hasAlpha ? PADDING + BAR_WIDTH : 0) + PADDING * 2 + 8;
		var totalHeight = PICKER_SIZE + PADDING * 2 + 16 + 28;
		gui.fill(pickerX - 4, pickerY - 4, pickerX + totalWidth + 4, pickerY + totalHeight + 4, 0xF0101010);
		gui.renderOutline(pickerX - 4, pickerY - 4, totalWidth + 8, totalHeight + 8, 0xFF404040);
		gui.drawString(font, title, svX, pickerY + PADDING, 0xFFFFFF);
		if (cachedHue != hue) rebuildSBCache();
		renderSBSquare(gui, svX, svY);
		var hueBarX = getHueBarX();
		renderHueBar(gui, hueBarX, svY);
		if (hasAlpha) renderAlphaBar(gui, getAlphaBarX(), svY);
		renderSelectors(gui, hueBarX);
		// Keep widget rendering order controlled in this screen.
		for (var child : children()) if (child instanceof AbstractWidget widget) widget.render(gui, mouseX, mouseY, delta);
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
	private void renderSelectors(GuiGraphics gui, int hueBarX) {
		var sbSelectorX = svX + (int) (saturation * (PICKER_SIZE - 1));
		var sbSelectorY = svY + (int) ((1f - brightness) * (PICKER_SIZE - 1));
		gui.renderOutline(sbSelectorX - 4, sbSelectorY - 4, 9, 9, 0xFFFFFFFF);
		gui.renderOutline(sbSelectorX - 3, sbSelectorY - 3, 7, 7, 0xFF000000);
		var hueSelectorY = svY + (int) (hue * (PICKER_SIZE - 1));
		renderHorizontalArrow(gui, hueBarX, hueSelectorY);
		if (hasAlpha) {
			var alphaSelectorY = svY + (int) ((1f - alpha / 255f) * (PICKER_SIZE - 1));
			renderHorizontalArrow(gui, getAlphaBarX(), alphaSelectorY);
		}
	}
	private int getHueBarX() {
		return svX + PICKER_SIZE + PADDING;
	}
	private int getAlphaBarX() {
		return getHueBarX() + BAR_WIDTH + PADDING;
	}
	private void renderHorizontalArrow(GuiGraphics gui, int x, int y) {
		gui.fill(x - 3, y - 1, x, y + 2, 0xFFFFFFFF);
		gui.fill(x - 2, y, x, y + 1, 0xFF000000);
		gui.fill(x + BAR_WIDTH, y - 1, x + BAR_WIDTH + 3, y + 2, 0xFFFFFFFF);
		gui.fill(x + BAR_WIDTH, y, x + BAR_WIDTH + 2, y + 1, 0xFF000000);
	}
	private void renderSBSquare(GuiGraphics gui, int x, int y) {
		var blockSize = 4;
		for (var by = 0; by < PICKER_SIZE; by += blockSize)
			for (var bx = 0; bx < PICKER_SIZE; bx += blockSize) {
				var color = sbPixels[by * PICKER_SIZE + bx];
				var endX = Math.min(bx + blockSize, PICKER_SIZE);
				var endY = Math.min(by + blockSize, PICKER_SIZE);
				gui.fill(x + bx, y + by, x + endX, y + endY, color);
			}
		gui.renderOutline(x, y, PICKER_SIZE, PICKER_SIZE, 0xFF000000);
	}
	private void renderHueBar(GuiGraphics gui, int x, int y) {
		var step = 4;
		for (var dy = 0; dy < PICKER_SIZE; dy += step) {
			var h = (float) dy / (PICKER_SIZE - 1);
			var color = 0xFF000000 | Color.HSBtoRGB(h, 1f, 1f) & 0xFFFFFF;
			gui.fill(x, y + dy, x + BAR_WIDTH, y + Math.min(dy + step, PICKER_SIZE), color);
		}
		gui.renderOutline(x, y, BAR_WIDTH, PICKER_SIZE, 0xFF000000);
	}
	private void renderAlphaBar(GuiGraphics gui, int x, int y) {
		var baseColor = colorFromHSB() & 0xFFFFFF;
		var step = 4;
		for (var dy = 0; dy < PICKER_SIZE; dy += step) {
			var a = 255 - dy * 255 / (PICKER_SIZE - 1);
			gui.fill(x, y + dy, x + BAR_WIDTH, y + Math.min(dy + step, PICKER_SIZE), a << 24 | baseColor);
		}
		gui.renderOutline(x, y, BAR_WIDTH, PICKER_SIZE, 0xFF000000);
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
		if (getSBArea().contains(mouseX, mouseY)) {
			setFocused(null);
			draggingSV = true;
			updateFromMouse((int) mouseX, (int) mouseY);
			return true;
		}
		var hueBarArea = getHueBarArea();
		if (hueBarArea.contains(mouseX, mouseY)) {
			setFocused(null);
			draggingHue = true;
			updateFromMouse((int) mouseX, (int) mouseY);
			return true;
		}
		if (hasAlpha && getAlphaBarArea().contains(mouseX, mouseY)) {
			setFocused(null);
			draggingAlpha = true;
			updateFromMouse((int) mouseX, (int) mouseY);
			return true;
		}
		var handled = super.mouseClicked(mouseX, mouseY, button);
		if (!handled) setFocused(null);
		return handled;
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (button == 0 && (draggingSV || draggingHue || draggingAlpha)) {
			updateFromMouse((int) mouseX, (int) mouseY);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0) {
			draggingSV = false;
			draggingHue = false;
			draggingAlpha = false;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}
	private void updateFromMouse(int mouseX, int mouseY) {
		if (draggingSV) {
			saturation = Mth.clamp((float) (mouseX - svX) / (PICKER_SIZE - 1), 0f, 1f);
			brightness = Mth.clamp(1f - (float) (mouseY - svY) / (PICKER_SIZE - 1), 0f, 1f);
		} else if (draggingHue) hue = Mth.clamp((float) (mouseY - svY) / (PICKER_SIZE - 1), 0f, 1f);
		else if (draggingAlpha) alpha = 255 - Mth.clamp((mouseY - svY) * 255 / (PICKER_SIZE - 1), 0, 255);
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
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		var step = hasShiftDown() ? 0.05f : 0.01f;
		var intStep = hasShiftDown() ? 16 : 1;
		assert minecraft != null;
		var window = minecraft.getWindow().getWindow();
		var up = InputConstants.isKeyDown(window, InputConstants.KEY_UP);
		var down = InputConstants.isKeyDown(window, InputConstants.KEY_DOWN);
		var left = InputConstants.isKeyDown(window, InputConstants.KEY_LEFT);
		var right = InputConstants.isKeyDown(window, InputConstants.KEY_RIGHT);
		var pageUp = InputConstants.isKeyDown(window, InputConstants.KEY_PAGEUP);
		var pageDown = InputConstants.isKeyDown(window, InputConstants.KEY_PAGEDOWN);
		var home = hasAlpha && InputConstants.isKeyDown(window, InputConstants.KEY_HOME);
		var end = hasAlpha && InputConstants.isKeyDown(window, InputConstants.KEY_END);
		var handled = up || down || left || right || pageUp || pageDown || home || end;
		if (hexInput != null && hexInput.isFocused() && handled) return super.keyPressed(keyCode, scanCode, modifiers);
		if (!handled) return super.keyPressed(keyCode, scanCode, modifiers);
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
	public void resize(@NotNull Minecraft minecraft, int width, int height) {
		if (parent != null) parent.resize(minecraft, width, height);
		super.resize(minecraft, width, height);
	}
}