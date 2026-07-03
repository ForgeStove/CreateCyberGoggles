package io.github.forgestove.create_cyber_goggles.config.client.gui.entry;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.*;
import io.github.forgestove.create_cyber_goggles.config.client.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public final class KeybindValueConfigEntry<C> extends ValueConfigEntry<C, Key> {
	private final Button bindButton;
	private boolean capturing;
	private CaptureCallback captureCallback = (e, c) -> {};
	private KeybindState state = KeybindState.BOUND;
	private List<Component> conflictUsages = List.of();
	public KeybindValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, Key> valueNode) {
		super(tab, valueNode);
		bindButton = Button.builder(
			Component.empty(), b -> {
				capturing = true;
				captureCallback.onCaptureStateChanged(this, true);
				this.tab.getScreen().refresh();
			}
		).size(WIDTH, HEIGHT).build();
		children.add(bindButton);
		refresh();
	}
	@Override
	public void refresh() {
		super.refresh();
		var keyText = getValue().getDisplayName().copy().withStyle(state.color());
		if (capturing) bindButton.setMessage(Component.literal("> ").append(keyText).append(" <"));
		else bindButton.setMessage(keyText);
		if (state == KeybindState.CONFLICT && !conflictUsages.isEmpty()) bindButton.setTooltip(Tooltip.create(getConflictTooltip()));
		else bindButton.setTooltip(null);
	}
	private Component getConflictTooltip() {
		var usedBy = Component.empty();
		for (var i = 0; i < conflictUsages.size(); i++) {
			if (i > 0) usedBy = usedBy.append(Component.literal(", "));
			usedBy = usedBy.append(conflictUsages.get(i));
		}
		return Component.translatable("controls.keybinds.duplicateKeybinds", usedBy);
	}
	public void setCaptureCallback(CaptureCallback callback) {
		captureCallback = callback;
	}
	public Key getBoundKey() {
		return getValue();
	}
	public Component getDisplayTitle() {
		return valueNode.getTitle();
	}
	public void setKeybindState(KeybindState state) {
		this.state = state;
	}
	public void setConflictUsages(List<Component> conflictUsages) {
		this.conflictUsages = conflictUsages;
	}
	public boolean handleCaptureKey(int keyCode) {
		if (!capturing) return false;
		if (keyCode == InputConstants.KEY_ESCAPE) setValue(InputConstants.UNKNOWN);
		else setValue(Type.KEYSYM.getOrCreate(keyCode));
		capturing = false;
		captureCallback.onCaptureStateChanged(this, false);
		tab.getScreen().refresh();
		return true;
	}
	public boolean handleCaptureMouse(int button) {
		if (!capturing) return false;
		setValue(Type.MOUSE.getOrCreate(button));
		capturing = false;
		captureCallback.onCaptureStateChanged(this, false);
		tab.getScreen().refresh();
		return true;
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
		renderGui(gui, y, x, width, mouseX, mouseY, delta, undoButton, resetButton, bindButton);
	}
	public enum KeybindState {
		UNBOUND(ChatFormatting.DARK_GRAY),
		CONFLICT(ChatFormatting.YELLOW),
		BOUND(ChatFormatting.GREEN);
		private final ChatFormatting color;
		KeybindState(ChatFormatting color) {
			this.color = color;
		}
		public ChatFormatting color() {
			return color;
		}
	}
	@FunctionalInterface
	public interface CaptureCallback {
		void onCaptureStateChanged(KeybindValueConfigEntry<?> entry, boolean capturing);
	}
}
