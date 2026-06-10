package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
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
			CommonComponents.GUI_NO.copy().withStyle(ChatFormatting.RED),
			getValue()
		).displayOnlyValue().create(0, 0, WIDTH, HEIGHT, node.getTitle(), (b, value) -> setValue(value));
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
	public void renderContent(@NotNull GuiGraphics gui, int mouseX, int mouseY, boolean hovered, float delta) {
		renderGui(gui, getY(), getX(), getWidth(), mouseX, mouseY, delta, undoButton, resetButton, valueButton);
	}
}
