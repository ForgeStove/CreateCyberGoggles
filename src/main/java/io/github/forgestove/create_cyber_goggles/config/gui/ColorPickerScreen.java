package io.github.forgestove.create_cyber_goggles.config.gui;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.forgestove.create_cyber_goggles.config.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;
/**
 * Color picker popup screen with optimized rendering
 */
public final class ColorPickerScreen extends Screen {
	private static final int PICKER_SIZE = 128;
	private static final int HUE_BAR_WIDTH = 16;
	private static final int ALPHA_BAR_WIDTH = 16;
	private static final int PADDING = 8;
	private static final int BAR_GAP = 6;
	private static final Pattern HEX_PATTERN = Pattern.compile("[0-9A-Fa-f]*");
	private final Screen parent;
	private final boolean hasAlpha;
	private final IntConsumer onColorSelected;
	private float hue = 0f;
	private float saturation = 1f;
	private float brightness = 1f;
	private int alpha = 255;
	private boolean draggingSV = false;
	private boolean draggingHue = false;
	private boolean draggingAlpha = false;
	private int pickerX, pickerY;
	private int svX, svY;
	// Cached color data for SB square
	private int[] sbPixels;
	private float cachedHue = -1f;
	// Hex input field
	private EditBox hexInput;
	private boolean updatingFromInput = false;
	public ColorPickerScreen(Screen parent, int initialColor, boolean hasAlpha, IntConsumer onColorSelected) {
		super(Translation.COLOR_PICKER_TOOLTIP);
		this.parent = parent;
		this.hasAlpha = hasAlpha;
		this.onColorSelected = onColorSelected;
		updateHSBFromColor(initialColor);
	}
	private void updateHSBFromColor(int color) {
		if (hasAlpha) alpha = ARGB32.alpha(color);
		var r = ARGB32.red(color);
		var g = ARGB32.green(color);
		var b = ARGB32.blue(color);
		var hsb = Color.RGBtoHSB(r, g, b, null);
		hue = hsb[0];
		saturation = hsb[1];
		brightness = hsb[2];
	}
	private int colorFromHSB() {
		var rgb = Color.HSBtoRGB(hue, saturation, brightness);
		if (hasAlpha) return alpha << 24 | rgb & 0xFFFFFF;
		return rgb & 0xFFFFFF;
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
	@Override
	protected void init() {
		var totalWidth = PICKER_SIZE + BAR_GAP + HUE_BAR_WIDTH + (hasAlpha ? BAR_GAP + ALPHA_BAR_WIDTH : 0);
		var totalHeight = PICKER_SIZE + PADDING + 24;
		pickerX = (width - totalWidth) / 2 - PADDING;
		pickerY = (height - totalHeight) / 2 - PADDING - 10;
		svX = pickerX + PADDING;
		svY = pickerY + PADDING + 16;
		var buttonY = svY + PICKER_SIZE + PADDING;
		var buttonWidth = 40;
		if (minecraft == null) return;
		// OK button
		addRenderableWidget(Button.builder(
			Component.translatable("gui.ok"), b -> {
				onColorSelected.accept(colorFromHSB());
				minecraft.setScreen(parent);
			}
		).bounds(svX, buttonY, buttonWidth, 20).build());
		// Cancel button
		addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> minecraft.setScreen(parent))
			.bounds(svX + buttonWidth + 2, buttonY, buttonWidth, 20)
			.build());
		// Hex input field (after buttons)
		var hexInputX = svX + buttonWidth * 2 + 6;
		var hexInputWidth = hasAlpha ? 70 : 55;
		hexInput = new EditBox(font, hexInputX, buttonY, hexInputWidth, 20, Component.literal("Hex"));
		hexInput.setMaxLength(hasAlpha ? 8 : 6);
		hexInput.setValue(formatHexColor());
		hexInput.setFilter(s -> HEX_PATTERN.matcher(s).matches());
		hexInput.setResponder(this::onHexInputChange);
		addRenderableWidget(hexInput);
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
		} catch (NumberFormatException ignored) {}
	}
	private void updateHexInput() {
		if (hexInput == null || updatingFromInput) return;
		updatingFromInput = true;
		hexInput.setValue(formatHexColor());
		updatingFromInput = false;
	}
	@Override
	public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		var totalWidth = PICKER_SIZE + BAR_GAP + HUE_BAR_WIDTH + (hasAlpha ? BAR_GAP + ALPHA_BAR_WIDTH : 0) + PADDING * 2 + 8;
		var totalHeight = PICKER_SIZE + PADDING * 2 + 16 + 28;
		// Render parent without mouse interaction to prevent hover state changes
		if (parent != null) parent.render(gui, -1, -1, delta);
		// Render blur background
		renderBlurredBackground(delta);
		// Background panel
		gui.fill(pickerX - 4, pickerY - 4, pickerX + totalWidth + 4, pickerY + totalHeight + 4, 0xF0101010);
		gui.renderOutline(pickerX - 4, pickerY - 4, totalWidth + 8, totalHeight + 8, 0xFF404040);
		// Title
		gui.drawString(font, title, svX, pickerY + PADDING, 0xFFFFFF);
		// Rebuild SB cache if hue changed
		if (cachedHue != hue) rebuildSBCache();
		// Render SB square from cache
		renderSBSquare(gui, svX, svY);
		// Hue bar
		var hueBarX = svX + PICKER_SIZE + BAR_GAP;
		renderHueBar(gui, hueBarX, svY);
		// Alpha bar
		if (hasAlpha) {
			var alphaBarX = hueBarX + HUE_BAR_WIDTH + BAR_GAP;
			renderAlphaBar(gui, alphaBarX, svY);
		}
		// SB selector crosshair
		var sbSelectorX = svX + (int) (saturation * (PICKER_SIZE - 1));
		var sbSelectorY = svY + (int) ((1 - brightness) * (PICKER_SIZE - 1));
		// Outer white circle
		gui.renderOutline(sbSelectorX - 4, sbSelectorY - 4, 9, 9, 0xFFFFFFFF);
		// Inner black circle
		gui.renderOutline(sbSelectorX - 3, sbSelectorY - 3, 7, 7, 0xFF000000);
		// Hue selector arrow
		var hueSelectorY = svY + (int) (hue * (PICKER_SIZE - 1));
		renderHorizontalArrow(gui, hueBarX, hueSelectorY, HUE_BAR_WIDTH);
		// Alpha selector arrow
		if (hasAlpha) {
			var alphaBarX = hueBarX + HUE_BAR_WIDTH + BAR_GAP;
			var alphaSelectorY = svY + (int) ((1 - alpha / 255f) * (PICKER_SIZE - 1));
			renderHorizontalArrow(gui, alphaBarX, alphaSelectorY, ALPHA_BAR_WIDTH);
		}
		// Color preview (right after hex input)
		var previewSize = 20;
		var previewX = hexInput.getX() + hexInput.getWidth() + 4;
		var previewY = svY + PICKER_SIZE + PADDING;
		var previewColor = colorFromHSB();
		gui.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, hasAlpha ? previewColor : 0xFF000000 | previewColor);
		gui.renderOutline(previewX, previewY, previewSize, previewSize, 0xFFA0A0A0);
		// Render widgets at same Z level
		for (var child : children())
			if (child instanceof AbstractWidget widget) widget.render(gui, mouseX, mouseY, delta);
	}
	private void renderHorizontalArrow(GuiGraphics gui, int x, int y, int width) {
		// Left arrow
		gui.fill(x - 3, y - 1, x, y + 2, 0xFFFFFFFF);
		gui.fill(x - 2, y, x, y + 1, 0xFF000000);
		// Right arrow
		gui.fill(x + width, y - 1, x + width + 3, y + 2, 0xFFFFFFFF);
		gui.fill(x + width, y, x + width + 2, y + 1, 0xFF000000);
	}
	private void renderSBSquare(GuiGraphics gui, int x, int y) {
		// Render cached pixels in larger blocks for better performance
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
			gui.fill(x, y + dy, x + HUE_BAR_WIDTH, y + Math.min(dy + step, PICKER_SIZE), color);
		}
		gui.renderOutline(x, y, HUE_BAR_WIDTH, PICKER_SIZE, 0xFF000000);
	}
	private void renderAlphaBar(GuiGraphics gui, int x, int y) {
		var baseColor = colorFromHSB() & 0xFFFFFF;
		var step = 4;
		for (var dy = 0; dy < PICKER_SIZE; dy += step) {
			var a = 255 - dy * 255 / (PICKER_SIZE - 1);
			var color = a << 24 | baseColor;
			gui.fill(x, y + dy, x + ALPHA_BAR_WIDTH, y + Math.min(dy + step, PICKER_SIZE), color);
		}
		gui.renderOutline(x, y, ALPHA_BAR_WIDTH, PICKER_SIZE, 0xFF000000);
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
		// Check SB square
		if (mouseX >= svX && mouseX < svX + PICKER_SIZE && mouseY >= svY && mouseY < svY + PICKER_SIZE) {
			draggingSV = true;
			updateFromMouse((int) mouseX, (int) mouseY);
			return true;
		}
		// Check Hue bar
		var hueBarX = svX + PICKER_SIZE + BAR_GAP;
		if (mouseX >= hueBarX && mouseX < hueBarX + HUE_BAR_WIDTH && mouseY >= svY && mouseY < svY + PICKER_SIZE) {
			draggingHue = true;
			updateFromMouse((int) mouseX, (int) mouseY);
			return true;
		}
		// Check Alpha bar
		if (!hasAlpha) return super.mouseClicked(mouseX, mouseY, button);
		var alphaBarX = hueBarX + HUE_BAR_WIDTH + BAR_GAP;
		if (mouseX >= alphaBarX && mouseX < alphaBarX + ALPHA_BAR_WIDTH && mouseY >= svY && mouseY < svY + PICKER_SIZE) {
			draggingAlpha = true;
			updateFromMouse((int) mouseX, (int) mouseY);
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
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
		var step = 0.005f; // 0.5% per scroll - more precise
		var intStep = 1; // 1 alpha per scroll - most precise
		// Check if mouse is over Hue bar
		var hueBarX = svX + PICKER_SIZE + BAR_GAP;
		if (mouseX >= hueBarX && mouseX < hueBarX + HUE_BAR_WIDTH && mouseY >= svY && mouseY < svY + PICKER_SIZE) {
			hue = Mth.clamp(hue - (float) scrollY * step, 0f, 1f);
			updateHexInput();
			return true;
		}
		// Check if mouse is over Alpha bar
		if (hasAlpha) {
			var alphaBarX = hueBarX + HUE_BAR_WIDTH + BAR_GAP;
			if (mouseX >= alphaBarX && mouseX < alphaBarX + ALPHA_BAR_WIDTH && mouseY >= svY && mouseY < svY + PICKER_SIZE) {
				alpha = Mth.clamp(alpha + (int) (scrollY * intStep), 0, 255);
				updateHexInput();
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		var step = hasShiftDown() ? 0.05f : 0.01f;
		var intStep = hasShiftDown() ? 15 : 3;
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
		if (up) brightness = Mth.clamp(brightness + step, 0f, 1f);
		if (down) brightness = Mth.clamp(brightness - step, 0f, 1f);
		if (left) saturation = Mth.clamp(saturation - step, 0f, 1f);
		if (right) saturation = Mth.clamp(saturation + step, 0f, 1f);
		if (pageUp) hue = Mth.clamp(hue - step, 0f, 1f);
		if (pageDown) hue = Mth.clamp(hue + step, 0f, 1f);
		if (home) alpha = Mth.clamp(alpha + intStep, 0, 255);
		if (end) alpha = Mth.clamp(alpha - intStep, 0, 255);
		if (handled) {
			updateHexInput();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	@Override
	public void resize(@NotNull Minecraft minecraft, int width, int height) {
		// Resize parent screen first to keep it in sync
		if (parent != null) parent.resize(minecraft, width, height);
		super.resize(minecraft, width, height);
	}
}
