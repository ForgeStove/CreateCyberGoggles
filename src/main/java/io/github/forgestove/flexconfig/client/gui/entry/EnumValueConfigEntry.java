package io.github.forgestove.flexconfig.client.gui.entry;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.gui.*;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
@SuppressWarnings("rawtypes")
public final class EnumValueConfigEntry<C> extends CapturableValueConfigEntry<C, Enum> {
	private final Button dropdownButton;
	private final String enumClassName;
	public EnumValueConfigEntry(ConfigCategoryTab<C, Enum> tab, ValueConfigNode<C, Enum> node) {
		super(tab, node);
		enumClassName = node.getValueType().getSimpleName();
		dropdownButton = Button.builder(getDisplayComponent(getValue()), this::openDropdown).size(WIDTH / 2, HEIGHT).build();
		children.add(dropdownButton);
		registerKeybindTask();
	}
	private Component getDisplayComponent(Enum<?> value) {
		return Component.translatable("%s.config.enum.%s.%s".formatted(tab.screen.root.modId(), enumClassName, value.name()));
	}
	private void openDropdown(Button button) {
		var mc = ClientUtil.mc;
		var screen = mc.screen;
		if (screen == null || screen instanceof EnumDropdownScreen) return;
		mc.setScreen(new EnumDropdownScreen(
			node.getValueType().getEnumConstants(),
			this::getValue,
			this::selectValue,
			this::getDisplayComponent,
			screen,
			dropdownButton
		));
	}
	@Override
	protected void registerKeybindTask() {
		registerTriggerAction(() -> {
			var values = node.getValueType().getEnumConstants();
			var nextIndex = (getValue().ordinal() + 1) % values.length;
			setValue(values[nextIndex]);
			var title = node.getTitle().copy().withStyle(ChatFormatting.WHITE);
			var valText = getDisplayComponent(getValue()).copy().withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
			if (ClientUtil.mc.player == null) return;
			ClientUtil.mc.player.displayClientMessage(title.append(": ").append(valText), true);
		});
	}
	private void selectValue(Enum<?> value) {
		setValue(value);
		dropdownButton.setMessage(getDisplayComponent(value));
	}
	@Override
	protected AbstractWidget getValueWidget() {
		return dropdownButton;
	}
	@Override
	public void refresh() {
		super.refresh();
		dropdownButton.setMessage(getDisplayComponent(getValue()));
	}
}
