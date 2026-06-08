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
	public ColorPreviewWidget(int x, int y, int width, int height, boolean hasAlpha, IntSupplier colorSupplier) {
		super(x, y, width, height, Component.empty());
		this.colorSupplier = colorSupplier;
		this.hasAlpha = hasAlpha;
		active = false;
	}
	@Override
	protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		var color = colorSupplier.getAsInt();
		if (!hasAlpha) color |= 0xFF000000;
		gui.fill(getX(), getY(), getX() + width, getY() + height, color);
		gui.renderOutline(getX(), getY(), width, height, 0xFF000000);
	}
	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {
		defaultButtonNarrationText(narration);
	}
}
