package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.gui.*;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;
public final class ColorValueConfigEntry<C> extends ValueConfigEntry<C, Integer, Integer> {
	public static final Pattern HEX_PATTERN = Pattern.compile("[0-9A-Fa-f]*");
	public static final int COLOR_PREVIEW_SIZE = 16;
	public final EditBox inputField;
	public final Button pickerButton;
	public final boolean hasAlpha;
	public ColorValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, Integer, Integer> valueNode, boolean hasAlpha) {
		super(tab, valueNode);
		this.hasAlpha = hasAlpha;
		this.inputField = new EditBox(tab.getMinecraft().font, 0, 0, 80, 18, this.valueNode.getTitle());
		this.inputField.setMaxLength(hasAlpha ? 8 : 6);
		this.inputField.setValue(formatColor(this.getValue()));
		this.inputField.setFilter(s -> HEX_PATTERN.matcher(s).matches());
		this.inputField.setResponder(this::onInputChange);
		this.pickerButton = Button.builder(Translation.COLOR_PICKER_LABEL, b -> openColorPicker()).bounds(0, 0, 20, 20).build();
		this.children.add(0, this.inputField);
		this.children.add(this.pickerButton);
	}
	private String formatColor(int color) {
		if (hasAlpha) return String.format("%08X", color);
		else return String.format("%06X", color & 0xFFFFFF);
	}
	private void openColorPicker() {
		var mc = this.tab.getMinecraft();
		mc.setScreen(new ColorPickerScreen(
			mc.screen, this.getValue(), hasAlpha, newColor -> {
			this.setValue(newColor);
			this.inputField.setValue(formatColor(newColor));
		}
		));
	}
	@Override
	public void refresh() {
		if (this.valueNode.validate(this.tab.getConfig()) == null) {
			var valueStr = formatColor(this.getValue());
			if (!this.inputField.getValue().equalsIgnoreCase(valueStr)) this.inputField.setValue(valueStr);
			this.inputField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY));
		} else this.inputField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY.withColor(ChatFormatting.RED)));
		super.refresh();
	}
	private void onInputChange(String value) {
		try {
			var color = (int) Long.parseLong(value, 16);
			this.setValue(color);
		} catch (NumberFormatException e) {
			// Ignore
		}
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
		this.renderLabel(guiGraphics, x, y, entryWidth);
		var rightEdge = x + entryWidth;
		var colorPreviewX = rightEdge - COLOR_PREVIEW_SIZE - this.undoButton.getWidth() - this.resetButton.getWidth() - 4;
		// Color preview box
		int currentColor = this.getValue();
		guiGraphics.fill(
			colorPreviewX,
			y + 2,
			colorPreviewX + COLOR_PREVIEW_SIZE,
			y + 2 + COLOR_PREVIEW_SIZE,
			hasAlpha ? currentColor : 0xFF000000 | currentColor
		);
		guiGraphics.renderOutline(colorPreviewX, y + 2, COLOR_PREVIEW_SIZE, COLOR_PREVIEW_SIZE, 0xFFA0A0A0);
		// Picker button
		this.pickerButton.setX(colorPreviewX - 24);
		this.pickerButton.setY(y);
		// Input field
		this.inputField.setX(this.pickerButton.getX() - 75);
		this.inputField.setY(y + 1);
		this.inputField.setWidth(70);
		// Reset and undo buttons
		this.resetButton.setX(colorPreviewX + COLOR_PREVIEW_SIZE + 2);
		this.resetButton.setY(y);
		this.undoButton.setX(this.resetButton.getX() + this.resetButton.getWidth() + 2);
		this.undoButton.setY(y);
		this.inputField.render(guiGraphics, mouseX, mouseY, delta);
		this.pickerButton.render(guiGraphics, mouseX, mouseY, delta);
		this.resetButton.render(guiGraphics, mouseX, mouseY, delta);
		this.undoButton.render(guiGraphics, mouseX, mouseY, delta);
	}
	/**
	 * Color picker popup screen with optimized rendering
	 */
	private static class ColorPickerScreen extends Screen {
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
		protected ColorPickerScreen(Screen parent, int initialColor, boolean hasAlpha, IntConsumer onColorSelected) {
			super(Translation.COLOR_PICKER_TOOLTIP);
			this.parent = parent;
			this.hasAlpha = hasAlpha;
			this.onColorSelected = onColorSelected;
			updateHSBFromColor(initialColor);
		}
		private void updateHSBFromColor(int color) {
			if (hasAlpha) this.alpha = FastColor.ARGB32.alpha(color);
			var r = FastColor.ARGB32.red(color);
			var g = FastColor.ARGB32.green(color);
			var b = FastColor.ARGB32.blue(color);
			var hsb = Color.RGBtoHSB(r, g, b, null);
			this.hue = hsb[0];
			this.saturation = hsb[1];
			this.brightness = hsb[2];
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
			pickerX = (this.width - totalWidth) / 2 - PADDING;
			pickerY = (this.height - totalHeight) / 2 - PADDING - 10;
			svX = pickerX + PADDING;
			svY = pickerY + PADDING + 16;
			var buttonY = svY + PICKER_SIZE + PADDING;
			var buttonWidth = 40;
			if (minecraft == null) return;
			// OK button
			this.addRenderableWidget(Button.builder(
				Component.translatable("gui.ok"), b -> {
					onColorSelected.accept(colorFromHSB());
					this.minecraft.setScreen(parent);
				}
			).bounds(svX, buttonY, buttonWidth, 20).build());
			// Cancel button
			this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> this.minecraft.setScreen(parent))
				.bounds(svX + buttonWidth + 2, buttonY, buttonWidth, 20)
				.build());
			// Hex input field (after buttons)
			var hexInputX = svX + buttonWidth * 2 + 6;
			var hexInputWidth = hasAlpha ? 70 : 55;
			hexInput = new EditBox(this.font, hexInputX, buttonY, hexInputWidth, 20, Component.literal("Hex"));
			hexInput.setMaxLength(hasAlpha ? 8 : 6);
			hexInput.setValue(formatHexColor());
			hexInput.setFilter(s -> HEX_PATTERN.matcher(s).matches());
			hexInput.setResponder(this::onHexInputChange);
			this.addRenderableWidget(hexInput);
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
			if (hexInput != null && !updatingFromInput) {
				updatingFromInput = true;
				hexInput.setValue(formatHexColor());
				updatingFromInput = false;
			}
		}
		@Override
		public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
			var totalWidth = PICKER_SIZE + BAR_GAP + HUE_BAR_WIDTH + (hasAlpha ? BAR_GAP + ALPHA_BAR_WIDTH : 0) + PADDING * 2 + 8;
			var totalHeight = PICKER_SIZE + PADDING * 2 + 16 + 28;
			// Render parent without mouse interaction to prevent hover state changes
			if (parent != null) parent.render(guiGraphics, -1, -1, delta);
			// Render background overlay (1.20.1 doesn't have renderBlurredBackground)
			guiGraphics.fill(0, 0, this.width, this.height, 0x80000000);
			// Background panel
			guiGraphics.fill(pickerX - 4, pickerY - 4, pickerX + totalWidth + 4, pickerY + totalHeight + 4, 0xF0101010);
			guiGraphics.renderOutline(pickerX - 4, pickerY - 4, totalWidth + 8, totalHeight + 8, 0xFF404040);
			// Title
			guiGraphics.drawString(this.font, this.title, svX, pickerY + PADDING, 0xFFFFFF);
			// Rebuild SB cache if hue changed
			if (cachedHue != hue) rebuildSBCache();
			// Render SB square from cache
			renderSBSquare(guiGraphics, svX, svY);
			// Hue bar
			var hueBarX = svX + PICKER_SIZE + BAR_GAP;
			renderHueBar(guiGraphics, hueBarX, svY);
			// Alpha bar
			if (hasAlpha) {
				var alphaBarX = hueBarX + HUE_BAR_WIDTH + BAR_GAP;
				renderAlphaBar(guiGraphics, alphaBarX, svY);
			}
			// SB selector crosshair
			var sbSelectorX = svX + (int) (saturation * (PICKER_SIZE - 1));
			var sbSelectorY = svY + (int) ((1 - brightness) * (PICKER_SIZE - 1));
			// Outer white circle
			guiGraphics.renderOutline(sbSelectorX - 4, sbSelectorY - 4, 9, 9, 0xFFFFFFFF);
			// Inner black circle
			guiGraphics.renderOutline(sbSelectorX - 3, sbSelectorY - 3, 7, 7, 0xFF000000);
			// Hue selector arrow
			var hueSelectorY = svY + (int) (hue * (PICKER_SIZE - 1));
			renderHorizontalArrow(guiGraphics, hueBarX, hueSelectorY, HUE_BAR_WIDTH);
			// Alpha selector arrow
			if (hasAlpha) {
				var alphaBarX = hueBarX + HUE_BAR_WIDTH + BAR_GAP;
				var alphaSelectorY = svY + (int) ((1 - alpha / 255f) * (PICKER_SIZE - 1));
				renderHorizontalArrow(guiGraphics, alphaBarX, alphaSelectorY, ALPHA_BAR_WIDTH);
			}
			// Color preview (right after hex input)
			var previewSize = 20;
			var previewX = hexInput.getX() + hexInput.getWidth() + 4;
			var previewY = svY + PICKER_SIZE + PADDING;
			var previewColor = colorFromHSB();
			guiGraphics.fill(
				previewX,
				previewY,
				previewX + previewSize,
				previewY + previewSize,
				hasAlpha ? previewColor : 0xFF000000 | previewColor
			);
			guiGraphics.renderOutline(previewX, previewY, previewSize, previewSize, 0xFFA0A0A0);
			// Render widgets at same Z level
			for (var child : this.children())
				if (child instanceof AbstractWidget widget) widget.render(guiGraphics, mouseX, mouseY, delta);
		}
		private void renderHorizontalArrow(GuiGraphics guiGraphics, int x, int y, int width) {
			// Left arrow
			guiGraphics.fill(x - 3, y - 1, x, y + 2, 0xFFFFFFFF);
			guiGraphics.fill(x - 2, y, x, y + 1, 0xFF000000);
			// Right arrow
			guiGraphics.fill(x + width, y - 1, x + width + 3, y + 2, 0xFFFFFFFF);
			guiGraphics.fill(x + width, y, x + width + 2, y + 1, 0xFF000000);
		}
		private void renderSBSquare(GuiGraphics guiGraphics, int x, int y) {
			// Render cached pixels in larger blocks for better performance
			var blockSize = 4;
			for (var by = 0; by < PICKER_SIZE; by += blockSize)
				for (var bx = 0; bx < PICKER_SIZE; bx += blockSize) {
					var color = sbPixels[by * PICKER_SIZE + bx];
					var endX = Math.min(bx + blockSize, PICKER_SIZE);
					var endY = Math.min(by + blockSize, PICKER_SIZE);
					guiGraphics.fill(x + bx, y + by, x + endX, y + endY, color);
				}
			guiGraphics.renderOutline(x, y, PICKER_SIZE, PICKER_SIZE, 0xFF000000);
		}
		private void renderHueBar(GuiGraphics guiGraphics, int x, int y) {
			var step = 4;
			for (var dy = 0; dy < PICKER_SIZE; dy += step) {
				var h = (float) dy / (PICKER_SIZE - 1);
				var color = 0xFF000000 | Color.HSBtoRGB(h, 1f, 1f) & 0xFFFFFF;
				guiGraphics.fill(x, y + dy, x + HUE_BAR_WIDTH, y + Math.min(dy + step, PICKER_SIZE), color);
			}
			guiGraphics.renderOutline(x, y, HUE_BAR_WIDTH, PICKER_SIZE, 0xFF000000);
		}
		private void renderAlphaBar(GuiGraphics guiGraphics, int x, int y) {
			var baseColor = colorFromHSB() & 0xFFFFFF;
			var step = 4;
			for (var dy = 0; dy < PICKER_SIZE; dy += step) {
				var a = 255 - dy * 255 / (PICKER_SIZE - 1);
				var color = a << 24 | baseColor;
				guiGraphics.fill(x, y + dy, x + ALPHA_BAR_WIDTH, y + Math.min(dy + step, PICKER_SIZE), color);
			}
			guiGraphics.renderOutline(x, y, ALPHA_BAR_WIDTH, PICKER_SIZE, 0xFF000000);
		}
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button == 0) {
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
				if (hasAlpha) {
					var alphaBarX = hueBarX + HUE_BAR_WIDTH + BAR_GAP;
					if (mouseX >= alphaBarX && mouseX < alphaBarX + ALPHA_BAR_WIDTH && mouseY >= svY && mouseY < svY + PICKER_SIZE) {
						draggingAlpha = true;
						updateFromMouse((int) mouseX, (int) mouseY);
						return true;
					}
				}
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
		public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
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
			return super.mouseScrolled(mouseX, mouseY, scrollY);
		}
		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			var step = 0.01f; // 1% per key press
			var intStep = 3; // 3 alpha per key press
			// Shift for faster movement
			if (hasShiftDown()) {
				step = 0.05f;
				intStep = 15;
			}
			// Arrow keys for SB square (saturation: left/right, brightness: up/down)
			switch (keyCode) {
				case 265 -> { // UP
					brightness = Mth.clamp(brightness + step, 0f, 1f);
					updateHexInput();
					return true;
				}
				case 264 -> { // DOWN
					brightness = Mth.clamp(brightness - step, 0f, 1f);
					updateHexInput();
					return true;
				}
				case 263 -> { // LEFT
					saturation = Mth.clamp(saturation - step, 0f, 1f);
					updateHexInput();
					return true;
				}
				case 262 -> { // RIGHT
					saturation = Mth.clamp(saturation + step, 0f, 1f);
					updateHexInput();
					return true;
				}
				case 266 -> { // PAGE_UP - increase hue
					hue = Mth.clamp(hue - step, 0f, 1f);
					updateHexInput();
					return true;
				}
				case 267 -> { // PAGE_DOWN - decrease hue
					hue = Mth.clamp(hue + step, 0f, 1f);
					updateHexInput();
					return true;
				}
				case 268 -> { // HOME - increase alpha
					if (hasAlpha) {
						alpha = Mth.clamp(alpha + intStep, 0, 255);
						updateHexInput();
						return true;
					}
				}
				case 269 -> { // END - decrease alpha
					if (hasAlpha) {
						alpha = Mth.clamp(alpha - intStep, 0, 255);
						updateHexInput();
						return true;
					}
				}
			}
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
		@Override
		public void resize(@NotNull Minecraft minecraft, int width, int height) {
			// Resize parent screen first to keep it in sync
			if (this.parent != null) this.parent.resize(minecraft, width, height);
			super.resize(minecraft, width, height);
		}
	}
}
