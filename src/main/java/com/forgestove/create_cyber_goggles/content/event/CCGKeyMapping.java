package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.*;
import org.lwjgl.glfw.GLFW;
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public enum CCGKeyMapping {
	toggleDiving(GLFW.GLFW_KEY_UNKNOWN),
	openConfig(GLFW.GLFW_KEY_UNKNOWN),
	previewFilter(GLFW.GLFW_KEY_UNKNOWN);
	private final String name;
	private final int defaultKeyCode;
	public KeyMapping keyMapping;
	CCGKeyMapping(int defaultKeyCode) {
		name = "key.%s.%s".formatted(CreateCyberGoggles.ID, name());
		this.defaultKeyCode = defaultKeyCode;
	}
	public static void register() {
		for (var key : values()) {
			key.keyMapping = new KeyMapping(key.name, key.defaultKeyCode, "key.categories.%s".formatted(CreateCyberGoggles.ID));
			KeyBindingHelper.registerKeyBinding(key.keyMapping);
		}
	}
	public boolean consumeClick() {
		return keyMapping.consumeClick();
	}
	public boolean isDown() {
		var keyCode = KeyBindingHelper.getBoundKeyOf(keyMapping).getValue();
		if (keyCode == GLFW.GLFW_KEY_UNKNOWN) return false;
		return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), keyCode) == GLFW.GLFW_PRESS;
	}
}
