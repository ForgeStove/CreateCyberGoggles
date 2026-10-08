package io.github.forgestove.flexconfig.client.gui.entry;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.gui.*;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.*;

import java.util.function.*;
public abstract class GenericValueConfigEntry<C, V> extends ValueConfigEntry<C, V> {
	private final ConfigEditBox inputField;
	private final Function<String, V> parser;
	private final Predicate<String> validator;
	private final Function<V, String> formatter;
	private boolean hasParseError = false;
	private boolean updatingFromCode = false;
	public GenericValueConfigEntry(
		ConfigCategoryTab<C, V> tab,
		ValueConfigNode<C, V> valueNode,
		Function<String, V> parser,
		Predicate<String> validator
	) {
		this(tab, valueNode, parser, validator, Object::toString);
	}
	public GenericValueConfigEntry(
		ConfigCategoryTab<C, V> tab,
		ValueConfigNode<C, V> valueNode,
		Function<String, V> parser,
		Predicate<String> validator,
		Function<V, String> formatter
	) {
		super(tab, valueNode);
		this.parser = parser;
		this.validator = validator;
		this.formatter = formatter;
		inputField = new ConfigEditBox(ClientUtil.mc.font, 0, 0, WIDTH, HEIGHT, node.getTitle());
		inputField.setValue(text(getValue()));
		inputField.setFilter(validator);
		inputField.setResponder(this::onInputChange);
		children.add(inputField);
	}
	/** 值 → 文本（列表等复合类型可覆写格式）。 */
	private String text(@Nullable V value) {
		return value == null ? "" : formatter.apply(value);
	}
	private void onInputChange(String value) {
		if (updatingFromCode) return;
		if (!validator.test(value)) {
			hasParseError = true;
			tab.screen.refresh();
			return;
		}
		try {
			setValue(parser.apply(value));
			hasParseError = false;
		} catch (Exception e) {
			hasParseError = true;
		}
		tab.screen.refresh();
	}
	@Override
	public void resetToDefault() {
		hasParseError = false;
		node.resetToDefault();
		updatingFromCode = true;
		inputField.setValue(text(getValue()));
		updatingFromCode = false;
		tab.screen.refresh();
	}
	@Override
	public void resetToActive() {
		hasParseError = false;
		node.resetToActive(tab.config);
		updatingFromCode = true;
		inputField.setValue(text(getValue()));
		updatingFromCode = false;
		tab.screen.refresh();
	}
	@Override
	public void refresh() {
		var hasError = hasParseError || node.validate(tab.config) != null;
		if (!hasError) {
			var valueStr = text(getValue());
			if (!isZero(inputField.getValue())) try {
				var parsed = parser.apply(inputField.getValue());
				if (!parsed.equals(getValue())) {
					updatingFromCode = true;
					inputField.setValue(valueStr);
					updatingFromCode = false;
				}
			} catch (Exception e) {
				updatingFromCode = true;
				inputField.setValue(valueStr);
				updatingFromCode = false;
			}
			inputField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY));
		} else inputField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY.withColor(ChatFormatting.RED)));
		super.refresh();
		if (!hasParseError) return;
		resetButton.active = true;
		undoButton.active = true;
	}
	public static boolean isZero(@NotNull String string) {
		return string.isEmpty() || string.equals("-");
	}
	@Override
	public boolean hasError() {
		return hasParseError || super.hasError();
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
		inputField.active = !isReadOnly();
		renderGui(gui, y, x, width, mouseX, mouseY, delta, lockButton, undoButton, resetButton, inputField);
	}
}
