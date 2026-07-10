package io.github.forgestove.config.client.gui.factory;
import net.minecraft.ChatFormatting;
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
