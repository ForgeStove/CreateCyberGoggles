package io.github.forgestove.create_cyber_goggles.event;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;
public enum CCGKeyMapping {
	openConfig(GLFW.GLFW_KEY_UNKNOWN),
	openStock(GLFW.GLFW_KEY_UNKNOWN),
	previewFilter(GLFW.GLFW_KEY_UNKNOWN),
	toggleDiving(GLFW.GLFW_KEY_UNKNOWN),
	showStress(GLFW.GLFW_KEY_TAB);
	private final KeyMapping keyMapping;
	CCGKeyMapping(int key) {
		keyMapping = new KeyMapping(CCG.ID + ".key." + name(), key, CCG.ID + ".categories.key");
	}
	public static void register(RegisterKeyMappingsEvent event) {
		for (var key : values()) event.register(key.get());
	}
	public KeyMapping get() {
		return keyMapping;
	}
	public boolean isDown() {
		return get().isDown();
	}
}
