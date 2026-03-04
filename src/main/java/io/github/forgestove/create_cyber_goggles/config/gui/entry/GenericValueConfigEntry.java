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
		this.inputField = new EditBox(tab.getMinecraft().font, 0, 0, 158, 18, this.valueNode.getTitle());
		this.inputField.setValue(this.getValue().toString());
		this.inputField.setFilter(validator);
		this.inputField.setResponder(this::onInputChange);
		this.children.add(this.inputField);
	}
	public static boolean isZero(@NotNull String string) {
		return string.isEmpty() || string.equals("-");
	}
	@Override
	public void refresh() {
		var hasError = hasParseError || this.valueNode.validate(this.tab.getConfig()) != null;
		if (!hasError) {
			var valueStr = this.getValue().toString();
			if (!this.inputField.getValue().equals(valueStr)) {
				updatingFromCode = true;
				this.inputField.setValue(valueStr);
				updatingFromCode = false;
			}
			this.inputField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY));
		} else this.inputField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY.withColor(ChatFormatting.RED)));
		super.refresh();
		if (hasParseError) {
			this.resetButton.active = true;
			this.undoButton.active = true;
		}
	}
	@Override
	public void resetToDefault() {
		hasParseError = false;
		this.valueNode.resetToDefault();
		updatingFromCode = true;
		this.inputField.setValue(this.getValue().toString());
		updatingFromCode = false;
		this.tab.getScreen().refresh();
	}
	@Override
	public void resetToActive() {
		hasParseError = false;
		this.valueNode.resetToActive(this.tab.getConfig());
		updatingFromCode = true;
		this.inputField.setValue(this.getValue().toString());
		updatingFromCode = false;
		this.tab.getScreen().refresh();
	}
	private void onInputChange(String value) {
		if (updatingFromCode) return;
		if (!validator.test(value)) {
			hasParseError = true;
			this.tab.getScreen().refresh();
			return;
		}
		try {
			this.setValue(parser.apply(value));
			hasParseError = false;
		} catch (Exception e) {
			hasParseError = true;
		}
		this.tab.getScreen().refresh();
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
		this.renderLabel(guiGraphics, x, y, entryWidth);
		this.inputField.setX(x + entryWidth - 158 - 1);
		this.inputField.setY(y + 1);
		this.resetButton.setX(x + entryWidth - this.resetButton.getWidth() - 2 - this.undoButton.getWidth());
		this.resetButton.setY(y);
		this.undoButton.setX(x + entryWidth - this.undoButton.getWidth());
		this.undoButton.setY(y);
		this.inputField.setWidth(158 - this.resetButton.getWidth() - 2 - this.undoButton.getWidth() - 2);
		if (this.tab.getMinecraft().font.isBidirectional()) {
			this.undoButton.setX(x);
			this.undoButton.setY(y);
			this.resetButton.setX(this.undoButton.getX() + this.undoButton.getWidth() + 2);
			this.resetButton.setY(y);
			this.inputField.setX(this.resetButton.getX() + this.resetButton.getWidth() + 2);
			this.inputField.setY(y + 1);
		} else {
			this.undoButton.setX(x + entryWidth - this.undoButton.getWidth());
			this.undoButton.setY(y);
			this.resetButton.setX(this.undoButton.getX() - this.resetButton.getWidth() - 2);
			this.resetButton.setY(y);
			this.inputField.setX(this.resetButton.getX() - this.inputField.getWidth() - 3);
			this.inputField.setY(y + 1);
		}
		this.inputField.render(guiGraphics, mouseX, mouseY, delta);
		this.resetButton.render(guiGraphics, mouseX, mouseY, delta);
		this.undoButton.render(guiGraphics, mouseX, mouseY, delta);
	}
}

