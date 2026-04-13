package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.gui.widget.EnumDropdownOverlayWidget;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
public final class EnumValueConfigEntry<C> extends ValueConfigEntry<C, Enum<?>, Enum<?>> {
	private final Button dropdownButton;
	private final String modId;
	private final String enumClassName;
	private final EnumDropdownOverlayWidget dropdownOverlay;
	public EnumValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, Enum<?>, Enum<?>> valueNode, String modId) {
		super(tab, valueNode);
		this.modId = modId;
		var enumValues = valueNode.getValueType().getEnumConstants();
		enumClassName = valueNode.getValueType().getSimpleName();
		dropdownButton = Button.builder(getDisplayComponent(getValue()), this::toggleDropdown).size(WIDTH, HEIGHT).build();
		dropdownOverlay = new EnumDropdownOverlayWidget(enumValues, this::getValue, this::selectValue, this::getDisplayComponent);
		children.add(dropdownButton);
	}
	private Component getDisplayComponent(Enum<?> value) {
		return Component.translatable(modId + ".config.enum." + enumClassName + "." + value.name());
	}
	private void toggleDropdown(Button button) {
		var wasExpanded = dropdownOverlay.expanded;
		dropdownOverlay.toggle();
		if (wasExpanded) dropdownButton.playDownSound(tab.getMinecraft().getSoundManager());
	}
	private void selectValue(Enum<?> value) {
		setValue(value);
		closeDropdown();
		dropdownButton.setMessage(getDisplayComponent(value));
	}
	public void closeDropdown() {
		if (!dropdownOverlay.expanded) return;
		dropdownOverlay.close();
		dropdownButton.playDownSound(tab.getMinecraft().getSoundManager());
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
		int entryWidth,
		int entryHeight,
		int mouseX,
		int mouseY,
		boolean hovered,
		float delta
	) {
		renderLabel(gui, x, y);
		var screenBottom = tab.getScreen().height - tab.getScreen().getFooterHeight();
		undoButton.setX(x + entryWidth - undoButton.getWidth());
		resetButton.setX(undoButton.getX() - resetButton.getWidth() - GAP);
		dropdownButton.setX(resetButton.getX() - dropdownButton.getWidth() - GAP);
		undoButton.setY(y);
		resetButton.setY(y);
		dropdownButton.setY(y);
		undoButton.render(gui, mouseX, mouseY, delta);
		resetButton.render(gui, mouseX, mouseY, delta);
		dropdownButton.render(gui, mouseX, mouseY, delta);
		dropdownOverlay.updateLayout(dropdownButton.getX(), y + HEIGHT + GAP, screenBottom);
	}
	/** Render the dropdown overlay - called after all entries are rendered */
	public void renderDropdownOverlay(@NotNull GuiGraphics gui, int mouseX, int mouseY) {
		dropdownOverlay.renderOverlay(gui, tab, mouseX, mouseY);
	}
	public boolean isExpanded() {
		return dropdownOverlay.expanded;
	}
	public boolean isMouseOverDropdown(double mouseX, double mouseY) {
		return dropdownOverlay.isMouseOver(mouseX, mouseY);
	}
	public boolean handleDropdownClick(double mouseX, double mouseY) {
		return dropdownOverlay.handleClick(mouseX, mouseY);
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (dropdownOverlay.expanded && isMouseOverDropdown(mouseX, mouseY)) return handleDropdownClick(mouseX, mouseY);
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		dropdownOverlay.stopDragging();
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!dropdownOverlay.handleDrag(mouseY)) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		return true;
	}
	public boolean handleDropdownScroll(double mouseX, double mouseY, double vertical) {
		return dropdownOverlay.handleScroll(mouseX, mouseY, vertical);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (handleDropdownScroll(mouseX, mouseY, vertical)) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
	}
}

