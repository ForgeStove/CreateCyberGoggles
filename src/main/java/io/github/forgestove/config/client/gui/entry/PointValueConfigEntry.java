package io.github.forgestove.config.client.gui.entry;
import io.github.forgestove.config.client.ClientUtil;
import io.github.forgestove.config.client.gui.*;
import io.github.forgestove.config.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.awt.Point;
import java.util.regex.Pattern;
public final class PointValueConfigEntry<C> extends ValueConfigEntry<C, Point> {
	public static final Pattern INTEGER_PATTERN = Pattern.compile("-?\\d*");
	public final ConfigEditBox xField;
	public final ConfigEditBox yField;
	private boolean updatingFromCode = false;
	public PointValueConfigEntry(ConfigCategoryTab<C, Point> tab, ValueConfigNode<C, Point> node) {
		super(tab, node);
		var width = (WIDTH - GAP) / 2;
		var font = ClientUtil.mc.font;
		var x = Component.literal("X");
		xField = new ConfigEditBox(font, 0, 0, width, HEIGHT, x);
		xField.setValue(String.valueOf(getValue().x));
		xField.setFilter(s -> INTEGER_PATTERN.matcher(s).matches());
		xField.setResponder(this::onXChange);
		xField.setTooltip(Tooltip.create(x));
		var y = Component.literal("Y");
		yField = new ConfigEditBox(font, 0, 0, width, HEIGHT, y);
		yField.setValue(String.valueOf(getValue().y));
		yField.setFilter(s -> INTEGER_PATTERN.matcher(s).matches());
		yField.setResponder(this::onYChange);
		yField.setTooltip(Tooltip.create(y));
		children.add(xField);
		children.add(yField);
	}
	private void onXChange(String value) {
		if (updatingFromCode) return;
		try {
			var x = value.isEmpty() || value.equals("-") ? 0 : Integer.parseInt(value);
			setValue(new Point(x, getValue().y));
		} catch (NumberFormatException ignored) {}
	}
	private void onYChange(String value) {
		if (updatingFromCode) return;
		try {
			var y = value.isEmpty() || value.equals("-") ? 0 : Integer.parseInt(value);
			setValue(new Point(getValue().x, y));
		} catch (NumberFormatException ignored) {}
	}
	@Override
	public void refresh() {
		var hasError = node.validate(tab.config) != null;
		if (!hasError) {
			var point = getValue();
			if (!xField.isFocused()) {
				updatingFromCode = true;
				xField.setValue(String.valueOf(point.x));
				updatingFromCode = false;
			}
			if (!yField.isFocused()) {
				updatingFromCode = true;
				yField.setValue(String.valueOf(point.y));
				updatingFromCode = false;
			}
			xField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY));
			yField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY));
		} else {
			xField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY.withColor(ChatFormatting.RED)));
			yField.setFormatter((s, i) -> FormattedCharSequence.forward(s, Style.EMPTY.withColor(ChatFormatting.RED)));
		}
		super.refresh();
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
		renderGui(gui, y, x, width, mouseX, mouseY, delta, lockButton, undoButton, resetButton, yField, xField);
		var locked = isLocked();
		xField.active = !locked;
		yField.active = !locked;
	}
}
