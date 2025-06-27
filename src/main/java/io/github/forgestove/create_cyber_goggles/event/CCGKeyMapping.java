package io.github.forgestove.create_cyber_goggles.event;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.*;
import org.lwjgl.glfw.GLFW;
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public enum CCGKeyMapping {
	openConfig(GLFW.GLFW_KEY_UNKNOWN),
	previewFilter(GLFW.GLFW_KEY_UNKNOWN),
	toggleDiving(GLFW.GLFW_KEY_UNKNOWN),
	showStress(GLFW.GLFW_KEY_TAB);
	private final String name;
	private final int defaultKeyCode;
	public KeyMapping keyMapping;
	CCGKeyMapping(int defaultKeyCode) {
		name = "key.%s.%s".formatted(CCG.ID, name());
		this.defaultKeyCode = defaultKeyCode;
	}
	public static void register() {
		for (var key : values()) {
			key.keyMapping = new KeyMapping(key.name, key.defaultKeyCode, "key.categories.%s".formatted(CCG.ID));
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
