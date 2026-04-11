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
		this.bindButton = Button.builder(
			Component.empty(), b -> {
				this.capturing = true;
				this.tab.getScreen().refresh();
			}
		).bounds(0, 0, 120, 20).build();
		this.children.add(this.bindButton);
		refresh();
	}
	public boolean isCapturing() {
		return this.capturing;
	}
	public boolean handleCaptureKey(int keyCode) {
		if (!this.capturing) return false;
		if (keyCode == InputConstants.KEY_ESCAPE) setValue(InputConstants.UNKNOWN);
		else setValue(Type.KEYSYM.getOrCreate(keyCode));
		this.capturing = false;
		this.tab.getScreen().refresh();
		return true;
	}
	public boolean handleCaptureMouse(int button) {
		if (!this.capturing) return false;
		setValue(Type.MOUSE.getOrCreate(button));
		this.capturing = false;
		this.tab.getScreen().refresh();
		return true;
	}
	@Override
	public void refresh() {
		super.refresh();
		var keyText = this.getValue().getDisplayName();
		if (this.capturing) this.bindButton.setMessage(Component.literal("> " + keyText.getString() + " <"));
		else this.bindButton.setMessage(keyText);
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
		this.renderLabel(guiGraphics, x, y, entryWidth);
		this.undoButton.setX(x + entryWidth - this.undoButton.getWidth());
		this.undoButton.setY(y);
		this.resetButton.setX(this.undoButton.getX() - this.resetButton.getWidth() - 2);
		this.resetButton.setY(y);
		this.bindButton.setWidth(120);
		this.bindButton.setX(this.resetButton.getX() - this.bindButton.getWidth() - 3);
		this.bindButton.setY(y);
		this.bindButton.render(guiGraphics, mouseX, mouseY, delta);
		this.resetButton.render(guiGraphics, mouseX, mouseY, delta);
		this.undoButton.render(guiGraphics, mouseX, mouseY, delta);
	}
}

