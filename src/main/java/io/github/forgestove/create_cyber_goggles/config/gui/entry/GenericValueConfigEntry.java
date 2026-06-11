package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.gui.*;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import org.jetbrains.annotations.NotNull;

import java.util.function.*;
public abstract class GenericValueConfigEntry<C, V> extends ValueConfigEntry<C, V> {
	private final ConfigEditBox inputField;
	private final Function<String, V> parser;
	private final Predicate<String> validator;
	private boolean hasParseError = false;
	private boolean updatingFromCode = false;
	public GenericValueConfigEntry(
		ConfigCategoryTab<C> tab,
		ValueConfigNode<C, V> valueNode,
		Function<String, V> parser,
		Predicate<String> validator
	) {
		super(tab, valueNode);
		this.parser = parser;
		this.validator = validator;
		inputField = new ConfigEditBox(this.tab.getMinecraft().font, 0, 0, WIDTH, HEIGHT, this.valueNode.getTitle());
		inputField.setValue(getValue().toString());
		inputField.setResponder(this::onInputChange);
		children.add(inputField);
	}
	public static boolean isZero(@NotNull String string) {
		return string.isEmpty() || string.equals("-");
	}
	@Override
	public void refresh() {
		var hasError = hasParseError || valueNode.validate(tab.getConfig()) != null;
		if (!hasError) {
			var valueStr = getValue().toString();
			if (!isZero(inputField.getValue()) && !inputField.getValue().equals(valueStr)) {
				updatingFromCode = true;
				inputField.setValue(valueStr);
				updatingFromCode = false;
			}
			inputField.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
		} else inputField.setTextColor(0xFF5555);
		super.refresh();
		if (!hasParseError) return;
		resetButton.active = true;
		undoButton.active = true;
	}
	@Override
	public void resetToDefault() {
		hasParseError = false;
		valueNode.resetToDefault();
		updatingFromCode = true;
		inputField.setValue(getValue().toString());
		updatingFromCode = false;
		tab.getScreen().refresh();
	}
	@Override
	public void resetToActive() {
		hasParseError = false;
		valueNode.resetToActive(tab.getConfig());
		updatingFromCode = true;
		inputField.setValue(getValue().toString());
		updatingFromCode = false;
		tab.getScreen().refresh();
	}
	private void onInputChange(String value) {
		if (updatingFromCode) return;
		if (!validator.test(value)) {
			hasParseError = true;
			tab.getScreen().refresh();
			return;
		}
		try {
			setValue(parser.apply(value));
			hasParseError = false;
		} catch (Exception e) {
			hasParseError = true;
		}
		tab.getScreen().refresh();
	}
	@Override
	public boolean hasError() {
		return hasParseError || super.hasError();
	}
	@Override
	public void extractContent(@NotNull GuiGraphicsExtractor gui, int mouseX, int mouseY, boolean hovered, float delta) {
		renderGui(gui, getY(), getX(), getWidth(), mouseX, mouseY, delta, undoButton, resetButton, inputField);
	}
}
