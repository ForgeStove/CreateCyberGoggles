package io.github.forgestove.config.client.gui.entry;
import io.github.forgestove.config.client.*;
import io.github.forgestove.config.client.gui.*;
import io.github.forgestove.config.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;
public final class ColorValueConfigEntry<C> extends ValueConfigEntry<C, Integer> {
	public static final Pattern HEX_PATTERN = Pattern.compile("[0-9A-Fa-f]*");
	public final ConfigEditBox inputField;
	public final Button pickerButton;
	public final ColorPreviewWidget previewWidget;
	public final boolean hasAlpha;
	public ColorValueConfigEntry(ConfigCategoryTab<C, Integer> tab, ValueConfigNode<C, Integer> node) {
		super(tab, node);
		hasAlpha = node.colorHasAlpha();
		previewWidget = new ColorPreviewWidget(0, 0, SIZE, SIZE, hasAlpha, this::getValue);
		inputField = new ConfigEditBox(ClientUtil.mc.font, 0, 0, WIDTH - 44, HEIGHT, this.node.getTitle());
		inputField.setMaxLength(getMaxLength());
		inputField.setValue(formatColor(getValue()));
		inputField.setFilter(s -> HEX_PATTERN.matcher(s).matches());
		inputField.setResponder(this::onInputChange);
		pickerButton = Button.builder(Translation.COLOR_PICKER_LABEL, b -> openColorPicker())
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
	private void openColorPicker() {
		var mc = ClientUtil.mc;
		var screen = new ColorPickerScreen(mc.screen, getValue(), hasAlpha, this::accept);
		mc.setScreen(screen);
	}
	private void accept(int newColor) {
		setValue(newColor);
		inputField.setValue(formatColor(newColor));
	}
	@Override
	public void refresh() {
		var maxLength = getMaxLength();
		var isValid = node.validate(tab.config) == null && inputField.getValue().length() == maxLength;
		if (isValid) {
			var valueStr = formatColor(getValue());
			if (!inputField.isFocused() && !inputField.getValue().equalsIgnoreCase(valueStr)) inputField.setValue(valueStr);
			inputField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY));
		} else inputField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY.withColor(ChatFormatting.RED)));
		super.refresh();
	}
	@Override
	public void render(
		@NotNull GuiGraphics gui,
		int index,
		int y,
		int x,
		int width,
		int height,
		int mouseX,
		int mouseY,
		boolean hovering,
		float delta
	) {
		renderGui(gui, y, x, width, mouseX, mouseY, delta, lockButton, undoButton, resetButton, pickerButton, inputField, previewWidget);
		var locked = isLocked();
		previewWidget.active = !locked;
		inputField.active = !locked;
		pickerButton.active = !locked;
	}
}
