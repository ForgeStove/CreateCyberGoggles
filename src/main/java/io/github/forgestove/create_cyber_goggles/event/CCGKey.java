package io.github.forgestove.create_cyber_goggles.event;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.*;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;
public enum CCGKey {
	openConfig(GLFW.GLFW_KEY_UNKNOWN),
	openStock(GLFW.GLFW_KEY_UNKNOWN),
	previewFilter(GLFW.GLFW_KEY_UNKNOWN),
	toggleDiving(GLFW.GLFW_KEY_UNKNOWN),
	showStress(GLFW.GLFW_KEY_TAB);
	public final KeyMapping keyMapping;
	CCGKey(int key) {
		keyMapping = new KeyMapping(CCG.ID + ".key." + name(), key, CCG.ID + ".categories.key");
	}
	public static void register(RegisterKeyMappingsEvent event) {
		for (var key : values()) event.register(key.keyMapping);
	}
	public boolean isKeyDown() {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keyMapping.getKey().getValue());
	}
}
