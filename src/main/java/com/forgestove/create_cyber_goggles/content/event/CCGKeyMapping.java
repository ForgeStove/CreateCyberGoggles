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
	private final int keyCode;
	public KeyMapping keyMapping;
	CCGKeyMapping(int defaultKey) {
		name = "key.%s.%s".formatted(CreateCyberGoggles.ID, name());
		keyCode = defaultKey;
	}
	public static void register() {
		for (var key : values()) {
			key.keyMapping = new KeyMapping(key.name, key.keyCode, "key.categories.%s".formatted(CreateCyberGoggles.ID));
			KeyBindingHelper.registerKeyBinding(key.keyMapping);
		}
	}
	public boolean consumeClick() {
		return keyMapping.consumeClick();
	}
	public boolean isDown() {
		return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), KeyBindingHelper.getBoundKeyOf(keyMapping).getValue())
			== GLFW.GLFW_PRESS;
	}
}
