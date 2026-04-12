package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
public final class BooleanValueConfigEntry<C> extends ValueConfigEntry<C, Boolean, Boolean> {
	private final CycleButton<Boolean> valueButton;
	public BooleanValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, Boolean, Boolean> valueNode) {
		super(tab, valueNode);
		valueButton = CycleButton.booleanBuilder(
				CommonComponents.GUI_YES.copy().withStyle(ChatFormatting.GREEN),
				CommonComponents.GUI_NO.copy().withStyle(ChatFormatting.RED)
			)
			.withInitialValue(getValue())
			.displayOnlyValue()
			.create(0, 0, WIDTH, HEIGHT, valueNode.getTitle(), (b, value) -> setValue(value));
		children.add(valueButton);
	}
	@Override
	public void refresh() {
		super.refresh();
		var value = getValue();
		if (!Objects.equals(valueButton.getValue(), value)) valueButton.setValue(value);
	}
	@Override
	public void render(
		@NotNull GuiGraphics graphics,
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
		renderLabel(graphics, x, y);
		undoButton.setX(x + entryWidth - undoButton.getWidth());
		resetButton.setX(undoButton.getX() - resetButton.getWidth() - 2);
		valueButton.setX(resetButton.getX() - valueButton.getWidth() - 2);
		valueButton.setY(y);
		resetButton.setY(y);
		undoButton.setY(y);
		valueButton.render(graphics, mouseX, mouseY, delta);
		resetButton.render(graphics, mouseX, mouseY, delta);
		undoButton.render(graphics, mouseX, mouseY, delta);
	}
}
