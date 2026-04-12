package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.*;
import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
public final class KeybindValueConfigEntry<C> extends ValueConfigEntry<C, Key, Key> {
	private final Button bindButton;
	private boolean capturing;
	public KeybindValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, Key, Key> valueNode) {
		super(tab, valueNode);
		bindButton = Button.builder(
			Component.empty(), b -> {
				capturing = true;
				this.tab.getScreen().refresh();
			}
		).size(WIDTH, HEIGHT).build();
		children.add(bindButton);
		refresh();
	}
	public boolean isCapturing() {
		return capturing;
	}
	public boolean handleCaptureKey(int keyCode) {
		if (!capturing) return false;
		if (keyCode == InputConstants.KEY_ESCAPE) setValue(InputConstants.UNKNOWN);
		else setValue(Type.KEYSYM.getOrCreate(keyCode));
		capturing = false;
		tab.getScreen().refresh();
		return true;
	}
	public boolean handleCaptureMouse(int button) {
		if (!capturing) return false;
		setValue(Type.MOUSE.getOrCreate(button));
		capturing = false;
		tab.getScreen().refresh();
		return true;
	}
	@Override
	public void refresh() {
		super.refresh();
		var keyText = getValue().getDisplayName();
		if (capturing) bindButton.setMessage(Component.literal("> " + keyText.getString() + " <"));
		else bindButton.setMessage(keyText);
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
		float partialTick
	) {
		renderGui(gui, y, x, width, mouseX, mouseY, partialTick, undoButton, resetButton, bindButton);
	}
}
