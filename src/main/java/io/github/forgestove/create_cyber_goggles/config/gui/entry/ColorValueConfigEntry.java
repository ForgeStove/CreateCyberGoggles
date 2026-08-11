package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.Translation;
import io.github.forgestove.create_cyber_goggles.config.gui.*;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;
public final class ColorValueConfigEntry<C> extends ValueConfigEntry<C, Integer> {
	public static final Pattern HEX_PATTERN = Pattern.compile("[0-9A-Fa-f]*");
	public final ConfigEditBox inputField;
	public final Button pickerButton;
	public final ColorPreviewWidget previewWidget;
	public final boolean hasAlpha;
	public ColorValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, Integer> node) {
		super(tab, node);
		hasAlpha = node.colorHasAlpha();
		previewWidget = new ColorPreviewWidget(0, 0, SIZE, SIZE, hasAlpha, this::getValue);
		inputField = new ConfigEditBox(tab.getMinecraft().font, 0, 0, WIDTH - 44, HEIGHT, valueNode.getTitle());
		inputField.setFilter(text -> HEX_PATTERN.matcher(text).matches());
		inputField.setMaxLength(getMaxLength());
		inputField.setValue(formatColor(getValue()));
		inputField.setResponder(this::onInputChange);
		pickerButton = Button.builder(Translation.COLOR_PICKER_LABEL, _ -> openColorPicker())
			.size(SIZE, SIZE)
			.tooltip(Tooltip.create(Translation.COLOR_PICKER_TOOLTIP))
			.build();
		children.add(previewWidget);
		children.add(inputField);
		children.add(pickerButton);
	}
	private int getMaxLength() {
		return hasAlpha ? 8 : 6;
	}
	private String formatColor(int color) {
		return String.format(hasAlpha ? "%08X" : "%06X", color);
	}
	private void onInputChange(String value) {
		var maxLength = getMaxLength();
		if (value.length() != maxLength) return;
		try {
			setValue(Integer.parseUnsignedInt(value, 16));
		} catch (NumberFormatException ignored) {}
	}
	private void accept(int newColor) {
		setValue(newColor);
		inputField.setValue(formatColor(newColor));
	}
	private void openColorPicker() {
		var mc = tab.getMinecraft();
		var screen = new ColorPickerScreen(mc.gui.screen(), getValue(), hasAlpha, this::accept);
		mc.gui.setScreen(screen);
	}
	@Override
	public void refresh() {
		var maxLength = getMaxLength();
		var isValid = valueNode.validate(tab.getConfig()) == null && inputField.getValue().length() == maxLength;
		if (isValid) {
			var valueStr = formatColor(getValue());
			if (!inputField.isFocused() && !inputField.getValue().equalsIgnoreCase(valueStr)) inputField.setValue(valueStr);
			inputField.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
		} else inputField.setTextColor(0xFF5555);
		super.refresh();
	}
	@Override
	public void extractContent(@NotNull GuiGraphicsExtractor gui, int mouseX, int mouseY, boolean hovered, float delta) {
		renderGui(gui, getY(), getX(), getWidth(), mouseX, mouseY, delta, undoButton, resetButton, pickerButton, inputField,
			previewWidget);
	}
}
