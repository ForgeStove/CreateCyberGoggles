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
		this.inputField = new EditBox(tab.getMinecraft().font, 0, 0, 80, 18, this.valueNode.getTitle());
		this.inputField.setMaxLength(hasAlpha ? 8 : 6);
		this.inputField.setValue(formatColor(this.getValue()));
		this.inputField.setFilter(s -> HEX_PATTERN.matcher(s).matches());
		this.inputField.setResponder(this::onInputChange);
		this.pickerButton = Button.builder(Translation.COLOR_PICKER_LABEL, b -> openColorPicker()).bounds(0, 0, 20, 20).build();
		this.children.add(this.inputField);
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
}
