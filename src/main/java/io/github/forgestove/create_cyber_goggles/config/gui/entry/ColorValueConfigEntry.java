package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.Translation;
import io.github.forgestove.create_cyber_goggles.config.gui.*;
import io.github.forgestove.create_cyber_goggles.config.gui.screen.ColorPickerScreen;
import io.github.forgestove.create_cyber_goggles.config.gui.widget.ColorPreviewWidget;
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
	public final EditBox inputField;
	public final Button pickerButton;
	public final ColorPreviewWidget previewWidget;
	public final boolean hasAlpha;
	public ColorValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, Integer, Integer> valueNode, boolean hasAlpha) {
		super(tab, valueNode);
		this.hasAlpha = hasAlpha;
		inputField = new EditBox(tab.getMinecraft().font, 0, 0, WIDTH - 44, HEIGHT, this.valueNode.getTitle());
		inputField.setMaxLength(hasAlpha ? 8 : 6);
		inputField.setValue(formatColor(getValue()));
		inputField.setFilter(s -> HEX_PATTERN.matcher(s).matches());
		inputField.setResponder(this::onInputChange);
		pickerButton = Button.builder(Translation.COLOR_PICKER_LABEL, b -> openColorPicker()).size(SIZE, SIZE).build();
		previewWidget = new ColorPreviewWidget(0, 0, SIZE, SIZE, this::getValue);
		children.add(inputField);
		children.add(pickerButton);
		children.add(previewWidget);
	}
	private String formatColor(int color) {
		return String.format(hasAlpha ? "%08X" : "%06X", color);
	}
	private void openColorPicker() {
		var mc = tab.getMinecraft();
		var screen = new ColorPickerScreen(
			mc.screen, getValue(), hasAlpha, newColor -> {
			setValue(newColor);
			inputField.setValue(formatColor(newColor));
		}
		);
		mc.setScreen(screen);
	}
	@Override
	public void refresh() {
		var maxLength = hasAlpha ? 8 : 6;
		var isValid = valueNode.validate(tab.getConfig()) == null && inputField.getValue().length() == maxLength;
		if (isValid) {
			var valueStr = formatColor(getValue());
			// 不要在用编辑时覆盖文本
			if (!inputField.isFocused() && !inputField.getValue().equalsIgnoreCase(valueStr)) inputField.setValue(valueStr);
			inputField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY));
		} else inputField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY.withColor(ChatFormatting.RED)));
		super.refresh();
	}
	private void onInputChange(String value) {
		// 仅当输入达到所需长度时，才应用该值
		var maxLength = hasAlpha ? 8 : 6;
		if (value.length() != maxLength) return;
		try {
			setValue(Integer.parseUnsignedInt(value, 16));
		} catch (NumberFormatException ignored) {}
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
		float partialTick
	) {
		renderGui(gui, y, x, width, mouseX, mouseY, partialTick, undoButton, resetButton, previewWidget, pickerButton, inputField);
	}
}
