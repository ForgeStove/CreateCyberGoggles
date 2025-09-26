package io.github.forgestove.create_cyber_goggles.core.event;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.*;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;
public enum CCGKey {
	openConfig(GLFW.GLFW_KEY_UNKNOWN),
	openStock(GLFW.GLFW_KEY_UNKNOWN),
	previewFilter(GLFW.GLFW_KEY_UNKNOWN),
	showStress(GLFW.GLFW_KEY_TAB),
	toggleDiving(GLFW.GLFW_KEY_UNKNOWN),
	toggleGoggle(GLFW.GLFW_KEY_UNKNOWN);
	public final KeyMapping keyMapping;
	CCGKey(int key) {
		keyMapping = new KeyMapping(CCG.ID + ".key." + name(), key, "key.categories." + CCG.ID);
	}
	public static void register(RegisterKeyMappingsEvent event) {
		for (var key : values()) event.register(key.keyMapping);
	}
	public boolean isKeyDown() {
		var value = keyMapping.getKey().getValue();
		return value != GLFW.GLFW_KEY_UNKNOWN && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), value);
	}
}
