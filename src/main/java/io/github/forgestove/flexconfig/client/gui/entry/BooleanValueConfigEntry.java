package io.github.forgestove.flexconfig.client.gui.entry;
import io.github.forgestove.flexconfig.client.*;
import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.*;

import java.util.Objects;
public final class BooleanValueConfigEntry<C> extends CapturableValueConfigEntry<C, Boolean> {
	private final CycleButton<Boolean> valueButton;
	public BooleanValueConfigEntry(ConfigCategoryTab<C, Boolean> tab, ValueConfigNode<C, Boolean> node) {
		super(tab, node);
		valueButton = CycleButton.booleanBuilder(
				Translation.ON_LABEL.copy().withStyle(ChatFormatting.GREEN),
				Translation.OFF_LABEL.copy().withStyle(ChatFormatting.RED)
			)
			.withInitialValue(getValue())
			.displayOnlyValue()
			.create(0, 0, WIDTH / 2, HEIGHT, node.getTitle(), (b, value) -> setValue(value));
		children.add(valueButton);
		registerKeybindTask();
	}
	@Override
	protected void registerKeybindTask() {
		registerTriggerAction(() -> {
			var newVal = !getValue();
			setValue(newVal);
			var title = node.getTitle().copy().withStyle(ChatFormatting.WHITE);
			var valText = newVal
				? Translation.ON_LABEL.copy().withStyle(ChatFormatting.GREEN)
				: Translation.OFF_LABEL.copy().withStyle(ChatFormatting.RED);
			if (ClientUtil.mc.player == null) return;
			ClientUtil.mc.player.displayClientMessage(title.append(": ").append(valText), true);
		});
	}
	@Override
	protected AbstractWidget getValueWidget() {
		return valueButton;
	}
	@Override
	public void refresh() {
		super.refresh();
		var value = getValue();
		if (!Objects.equals(valueButton.getValue(), value)) valueButton.setValue(value);
	}
}
