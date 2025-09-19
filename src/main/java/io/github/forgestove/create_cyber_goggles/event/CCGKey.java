package io.github.forgestove.create_cyber_goggles.event;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
public enum CCGKey {
	openConfig(GLFW.GLFW_KEY_UNKNOWN),
	openStock(GLFW.GLFW_KEY_UNKNOWN),
	previewFilter(GLFW.GLFW_KEY_UNKNOWN),
	toggleDiving(GLFW.GLFW_KEY_UNKNOWN),
	showStress(GLFW.GLFW_KEY_TAB);
	public final KeyMapping keyMapping;
	CCGKey(int key) {
		keyMapping = new KeyMapping(CCG.ID + ".key." + name(), key, "key.categories." + CCG.ID);
	}
	public static void register() {
		for (var key : values()) KeyBindingHelper.registerKeyBinding(key.keyMapping);
	}
	public boolean isKeyDown() {
		return keyMapping.isDown();
	}
}
