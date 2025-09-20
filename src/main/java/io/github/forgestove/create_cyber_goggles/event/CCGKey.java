package io.github.forgestove.create_cyber_goggles.event;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.*;
import org.lwjgl.glfw.GLFW;
public enum CCGKey {
	openConfig(GLFW.GLFW_KEY_UNKNOWN),
	openStock(GLFW.GLFW_KEY_UNKNOWN),
	previewFilter(GLFW.GLFW_KEY_UNKNOWN),
	toggleDiving(GLFW.GLFW_KEY_UNKNOWN),
	showStress(GLFW.GLFW_KEY_TAB);
	public final KeyMapping keyMapping;
	private boolean wasDown = false;
	private long pressStartTime;
	CCGKey(int key) {
		keyMapping = new KeyMapping(CCG.ID + ".key." + name(), key, "key.categories." + CCG.ID);
	}
	public static void register() {
		for (var key : values()) KeyBindingHelper.registerKeyBinding(key.keyMapping);
	}
	public boolean isKeyDown() {
		var value = keyMapping.key.getValue();
		var isDown = value != GLFW.GLFW_KEY_UNKNOWN && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), value);
		var currentTime = System.currentTimeMillis();
		if (isDown && !wasDown) {
			wasDown = true;
			pressStartTime = currentTime;
			return true;
		} else if (isDown) return currentTime - pressStartTime >= 500;
		else {
			wasDown = false;
			return false;
		}
	}
}
