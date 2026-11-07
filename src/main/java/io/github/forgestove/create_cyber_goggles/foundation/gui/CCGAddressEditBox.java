package io.github.forgestove.create_cyber_goggles.foundation.gui;
import com.simibubi.create.content.logistics.AddressEditBox;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
public class CCGAddressEditBox extends AddressEditBox {
	private final Screen screen;
	public CCGAddressEditBox(Screen screen, Font font, int x, int y, int width, int height, boolean anchorToBottom) {
		super(screen, font, x, y, width, height, anchorToBottom);
		this.screen = screen;
	}
	@Override
	public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		if (screen instanceof ScissorArea area)
			gui.enableScissor(area.scissorLeft(), area.scissorTop(), area.scissorRight(), area.scissorBottom());
		super.renderWidget(gui, mouseX, mouseY, partialTick);
		if (screen instanceof ScissorArea) gui.disableScissor();
	}
}
