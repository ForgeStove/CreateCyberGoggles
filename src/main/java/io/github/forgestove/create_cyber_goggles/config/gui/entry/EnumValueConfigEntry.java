package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.gui.*;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
public final class EnumValueConfigEntry<C> extends ValueConfigEntry<C, Enum<?>> {
	private final Button dropdownButton;
	private final String enumClassName;
	public EnumValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, Enum<?>> node) {
		super(tab, node);
		enumClassName = node.getValueType().getSimpleName();
		dropdownButton = Button.builder(getDisplayComponent(getValue()), this::openDropdown).size(WIDTH, HEIGHT).build();
		children.add(dropdownButton);
	}
	private Component getDisplayComponent(Enum<?> value) {
		return Component.translatable("%s.config.enum.%s.%s".formatted(tab.getScreen().root.modId, enumClassName, value.name()));
	}
	private void openDropdown(Button button) {
		var mc = tab.getMinecraft();
		var screen = mc.gui.screen();
		if (screen == null || screen instanceof EnumDropdownScreen) return;
		mc.gui.setScreen(new EnumDropdownScreen(
			valueNode.getValueType().getEnumConstants(),
			this::getValue,
			this::selectValue,
			this::getDisplayComponent,
			screen,
			dropdownButton,
			screen.height
		));
	}
	private void selectValue(Enum<?> value) {
		setValue(value);
		dropdownButton.setMessage(getDisplayComponent(value));
	}
	@Override
	public void refresh() {
		super.refresh();
		dropdownButton.setMessage(getDisplayComponent(getValue()));
	}
	@Override
	public void extractContent(@NotNull GuiGraphicsExtractor gui, int mouseX, int mouseY, boolean hovered, float delta) {
		renderGui(gui, getY(), getX(), getWidth(), mouseX, mouseY, delta, undoButton, resetButton, dropdownButton);
	}
}
