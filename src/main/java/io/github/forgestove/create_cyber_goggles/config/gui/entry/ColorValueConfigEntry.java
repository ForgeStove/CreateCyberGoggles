package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.Translation;
import io.github.forgestove.create_cyber_goggles.config.gui.*;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

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
		inputField = new EditBox(tab.getMinecraft().font, 0, 0, 80, 18, this.valueNode.getTitle());
		inputField.setMaxLength(hasAlpha ? 8 : 6);
		inputField.setValue(formatColor(getValue()));
		inputField.setFilter(s -> HEX_PATTERN.matcher(s).matches());
		inputField.setResponder(this::onInputChange);
		pickerButton = Button.builder(Translation.COLOR_PICKER_LABEL, b -> openColorPicker()).bounds(0, 0, 20, 20).build();
		children.add(inputField);
		children.add(pickerButton);
	}
	private String formatColor(int color) {
		if (hasAlpha) return String.format("%08X", color);
		return String.format("%06X", color & 0xFFFFFF);
	}
	private void openColorPicker() {
		var mc = tab.getMinecraft();
		mc.setScreen(new ColorPickerScreen(
			mc.screen, getValue(), hasAlpha, newColor -> {
			setValue(newColor);
			inputField.setValue(formatColor(newColor));
		}
		));
	}
	@Override
	public void refresh() {
		if (valueNode.validate(tab.getConfig()) == null) {
			var valueStr = formatColor(getValue());
			if (!inputField.getValue().equalsIgnoreCase(valueStr)) inputField.setValue(valueStr);
			inputField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY));
		} else inputField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY.withColor(ChatFormatting.RED)));
		super.refresh();
	}
	private void onInputChange(String value) {
		try {
			var color = (int) Long.parseLong(value, 16);
			setValue(color);
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
		renderLabel(guiGraphics, x, y);
		var rightEdge = x + entryWidth;
		var colorPreviewX = rightEdge - COLOR_PREVIEW_SIZE - undoButton.getWidth() - resetButton.getWidth() - 4;
		// Color preview box
		int currentColor = getValue();
		guiGraphics.fill(
			colorPreviewX,
			y + 2,
			colorPreviewX + COLOR_PREVIEW_SIZE,
			y + 2 + COLOR_PREVIEW_SIZE,
			hasAlpha ? currentColor : 0xFF000000 | currentColor
		);
		guiGraphics.renderOutline(colorPreviewX, y + 2, COLOR_PREVIEW_SIZE, COLOR_PREVIEW_SIZE, 0xFFA0A0A0);
		// Picker button
		pickerButton.setX(colorPreviewX - 24);
		pickerButton.setY(y);
		// Input field
		inputField.setX(pickerButton.getX() - 75);
		inputField.setY(y + 1);
		inputField.setWidth(70);
		// Reset and undo buttons
		resetButton.setX(colorPreviewX + COLOR_PREVIEW_SIZE + 2);
		resetButton.setY(y);
		undoButton.setX(resetButton.getX() + resetButton.getWidth() + 2);
		undoButton.setY(y);
		inputField.render(guiGraphics, mouseX, mouseY, delta);
		pickerButton.render(guiGraphics, mouseX, mouseY, delta);
		resetButton.render(guiGraphics, mouseX, mouseY, delta);
		undoButton.render(guiGraphics, mouseX, mouseY, delta);
	}
}
