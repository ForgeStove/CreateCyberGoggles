package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;
public final class IntegerValueConfigEntry<C> extends ValueConfigEntry<C, Integer, Integer> {
	private static final Pattern INTEGER_PATTERN = Pattern.compile("-?\\d*");
	private final EditBox inputField;
	private boolean hasParseError = false;
	private boolean updatingFromCode = false;
	public IntegerValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, Integer, Integer> valueNode) {
		super(tab, valueNode);
		this.inputField = new EditBox(tab.getMinecraft().font, 0, 0, 158, 18, this.valueNode.getTitle());
		this.inputField.setValue(this.getValue().toString());
		this.inputField.setFilter(s -> INTEGER_PATTERN.matcher(s).matches());
		this.inputField.setResponder(this::onInputChange);
		// Set hint if it has range
		if (valueNode.hasRange())
			this.inputField.setHint(Component.literal("[" + valueNode.getRangeMin() + ", " + valueNode.getRangeMax() + "]")
				.withStyle(ChatFormatting.DARK_GRAY));
		this.children.add(0, this.inputField);
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
		// Enable reset and undo when there's a parse error
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
		if (value.isEmpty() || value.equals("-")) {
			hasParseError = false;
			return;
		}
		try {
			this.setValue(Integer.parseInt(value));
			hasParseError = false;
		} catch (NumberFormatException e) {
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

