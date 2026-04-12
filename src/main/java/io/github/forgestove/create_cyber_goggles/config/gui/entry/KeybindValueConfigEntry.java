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
		).bounds(0, 0, WIDTH, HEIGHT).build();
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
		@NotNull GuiGraphics guiGraphics,
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
		renderLabel(guiGraphics, x, y, entryWidth);
		undoButton.setX(x + entryWidth - undoButton.getWidth());
		resetButton.setX(undoButton.getX() - resetButton.getWidth() - 2);
		bindButton.setX(resetButton.getX() - bindButton.getWidth() - 3);
		undoButton.setY(y);
		resetButton.setY(y);
		bindButton.setY(y);
		resetButton.render(guiGraphics, mouseX, mouseY, delta);
		undoButton.render(guiGraphics, mouseX, mouseY, delta);
		bindButton.render(guiGraphics, mouseX, mouseY, delta);
	}
}

