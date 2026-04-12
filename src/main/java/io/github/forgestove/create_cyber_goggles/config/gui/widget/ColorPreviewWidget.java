package io.github.forgestove.create_cyber_goggles.config.gui.widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntSupplier;
public final class ColorPreviewWidget extends AbstractWidget {
	private final IntSupplier colorSupplier;
	private final boolean hasAlpha;
	public ColorPreviewWidget(int x, int y, int width, int height, IntSupplier colorSupplier, boolean hasAlpha) {
		super(x, y, width, height, Component.empty());
		this.colorSupplier = colorSupplier;
		this.hasAlpha = hasAlpha;
		active = false;
	}
	@Override
	protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		var color = colorSupplier.getAsInt();
		gui.fill(getX(), getY(), getX() + width, getY() + height, hasAlpha ? color : 0xFF000000 | color);
		gui.renderOutline(getX(), getY(), width, height, 0xFFA0A0A0);
	}
	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {
		defaultButtonNarrationText(narration);
	}
}
