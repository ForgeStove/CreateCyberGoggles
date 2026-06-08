package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.gui.screen.EnumDropdownScreen;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
public final class EnumValueConfigEntry<C> extends ValueConfigEntry<C, Enum<?>> {
	private final Button dropdownButton;
	private final String modId;
	private final String enumClassName;
	public EnumValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, Enum<?>> node, String modId) {
		super(tab, node);
		this.modId = modId;
		enumClassName = node.getValueType().getSimpleName();
		dropdownButton = Button.builder(getDisplayComponent(getValue()), this::openDropdown).size(WIDTH, HEIGHT).build();
		children.add(dropdownButton);
	}
	private Component getDisplayComponent(Enum<?> value) {
		return Component.translatable(modId + ".config.enum." + enumClassName + "." + value.name());
	}
	private void openDropdown(Button button) {
		var mc = tab.getMinecraft();
		if (mc.screen instanceof EnumDropdownScreen) return; // already open
		var enumValues = valueNode.getValueType().getEnumConstants();
		if (mc.screen == null) return;
		var screen = new EnumDropdownScreen(
			enumValues,
			this::getValue,
			this::selectValue,
			this::getDisplayComponent,
			mc.screen,
			dropdownButton.getX(),
			dropdownButton.getY() + dropdownButton.getHeight(),
			dropdownButton.getWidth(),
			mc.screen.height
		);
		mc.setScreen(screen);
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
	public void render(
		@NotNull GuiGraphics gui,
		int index,
		int y,
		int x,
		int width,
		int height,
		int mouseX,
		int mouseY,
		boolean hovered,
		float delta
	) {
		renderGui(gui, y, x, width, mouseX, mouseY, delta, undoButton, resetButton, dropdownButton);
	}
}
