package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.function.*;
public abstract class GenericValueConfigEntry<C, T> extends ValueConfigEntry<C, T, T> {
	private final EditBox inputField;
	private final Function<String, T> parser;
	private final Predicate<String> validator;
	private boolean hasParseError = false;
	private boolean updatingFromCode = false;
	public GenericValueConfigEntry(
		ConfigCategoryTab<C> tab,
		ValueConfigNode<C, T, T> valueNode,
		Function<String, T> parser,
		Predicate<String> validator
	) {
		super(tab, valueNode);
		this.parser = parser;
		this.validator = validator;
		inputField = new EditBox(tab.getMinecraft().font, 0, 0, 100, 20, this.valueNode.getTitle());
		inputField.setValue(getValue().toString());
		inputField.setFilter(validator);
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
			if (!inputField.getValue().equals(valueStr)) {
				updatingFromCode = true;
				inputField.setValue(valueStr);
				updatingFromCode = false;
			}
			inputField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY));
		} else inputField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY.withColor(ChatFormatting.RED)));
		super.refresh();
		if (hasParseError) {
			resetButton.active = true;
			undoButton.active = true;
		}
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
		renderLabel(guiGraphics, x, y, entryWidth);
		inputField.setY(y);
		resetButton.setY(y);
		undoButton.setY(y);
		//		this.inputField.setWidth(158 - this.resetButton.getWidth() - 2 - this.undoButton.getWidth() - 2);
		if (tab.getMinecraft().font.isBidirectional()) {
			undoButton.setX(x);
			resetButton.setX(undoButton.getX() + undoButton.getWidth() + 2);
			inputField.setX(resetButton.getX() + resetButton.getWidth() + 2);
		} else {
			undoButton.setX(x + entryWidth - undoButton.getWidth());
			resetButton.setX(undoButton.getX() - resetButton.getWidth() - 2);
			inputField.setX(resetButton.getX() - inputField.getWidth() - 3);
		}
		undoButton.setY(y);
		resetButton.setY(y);
		inputField.setY(y);
		inputField.render(guiGraphics, mouseX, mouseY, delta);
		resetButton.render(guiGraphics, mouseX, mouseY, delta);
		undoButton.render(guiGraphics, mouseX, mouseY, delta);
	}
}

