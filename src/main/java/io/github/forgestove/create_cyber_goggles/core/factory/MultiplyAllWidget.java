package io.github.forgestove.create_cyber_goggles.core.factory;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.ExpressionParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleConsumer;
/** 常驻的「倍数输入 + 应用」控件，输入支持算式。 */
public final class MultiplyAllWidget {
	public static final int FIELD_WIDTH = 46;
	public static final int FIELD_HEIGHT = 14;
	private final EditBox field;
	private final IconButton button;
	public MultiplyAllWidget(int x, int y, DoubleConsumer onApply) {
		field = new Field(x, y);
		field.setValue("2");
		field.setFocused(false);
		button = new IconButton(x + FIELD_WIDTH + 4, y - 2, AllIcons.I_MTD_CLOSE);
		button.setToolTip(Component.translatable("create_cyber_goggles.gui.multiplier.tooltip"));
		button.withCallback(() -> {
			var factor = ExpressionParser.evaluate(field.getValue());
			if (factor == null || factor <= 0) return;
			onApply.accept(factor);
		});
	}
	public static int scale(int value, double factor, int min, int max) {
		if (value <= 0) return value;
		var result = value * factor;
		if (result >= max) return max;
		if (result <= min) return min;
		return (int) Math.round(result);
	}
	public EditBox field() {
		return field;
	}
	public IconButton button() {
		return button;
	}
	private static final class Field extends EditBox {
		private Field(int x, int y) {
			super(Minecraft.getInstance().font, x, y, FIELD_WIDTH, FIELD_HEIGHT, Component.empty());
			setBordered(false);
			setFilter(CCG.config.misc.expressionInput ? ExpressionParser::isAllowed : value -> value.chars().allMatch(Character::isDigit));
			setTextColor(0xFFFFFFFF);
		}
		@Override
		public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
			gui.fill(getX() - 2, getY() - 1, getX() + width + 2, getY() + height + 1, 0xC0181410);
			super.renderWidget(gui, mouseX, mouseY, partialTick);
		}
	}
}
