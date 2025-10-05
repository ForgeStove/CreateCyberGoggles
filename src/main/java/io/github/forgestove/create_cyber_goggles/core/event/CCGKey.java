package io.github.forgestove.create_cyber_goggles.core.event;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.KeyMapping;
import net.neoforged.jarjar.nio.util.Lazy;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;
public enum CCGKey {
	openConfig(GLFW.GLFW_KEY_UNKNOWN),
	openStock(GLFW.GLFW_KEY_UNKNOWN),
	previewFilter(GLFW.GLFW_KEY_UNKNOWN),
	showStress(GLFW.GLFW_KEY_TAB),
	toggleDiving(GLFW.GLFW_KEY_UNKNOWN),
	toggleGoggle(GLFW.GLFW_KEY_UNKNOWN);
	public final Lazy<KeyMapping> keyMapping;
	CCGKey(int key) {
		keyMapping = Lazy.of(new KeyMapping(CCG.ID + ".key." + name(), key, "key.categories." + CCG.ID));
	}
	public static void register(RegisterKeyMappingsEvent event) {
		for (var key : values()) event.register(key.keyMapping.get());
	}
	public boolean isDown() {
		return keyMapping.get().isDown();
	}
}
