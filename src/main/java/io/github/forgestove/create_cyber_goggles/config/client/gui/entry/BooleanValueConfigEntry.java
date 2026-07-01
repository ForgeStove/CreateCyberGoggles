package io.github.forgestove.create_cyber_goggles.config.client.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.client.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
public final class BooleanValueConfigEntry<C> extends ValueConfigEntry<C, Boolean> {
	private final CycleButton<Boolean> valueButton;
	public BooleanValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, Boolean> node) {
		super(tab, node);
		valueButton = CycleButton.booleanBuilder(
				CommonComponents.GUI_YES.copy().withStyle(ChatFormatting.GREEN),
				CommonComponents.GUI_NO.copy().withStyle(ChatFormatting.RED)
			)
			.withInitialValue(getValue())
			.displayOnlyValue()
			.create(0, 0, WIDTH, HEIGHT, node.getTitle(), (b, value) -> setValue(value));
		children.add(valueButton);
	}
	@Override
	public void refresh() {
		super.refresh();
		var value = getValue();
		if (Objects.equals(valueButton.getValue(), value)) return;
		valueButton.setValue(value);
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
		renderGui(gui, y, x, width, mouseX, mouseY, delta, lockButton, undoButton, resetButton, valueButton);
		valueButton.active = !isLocked();
	}
}
