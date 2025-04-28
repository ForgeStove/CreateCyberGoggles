package com.forgestove.create_cyber_goggles.event;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
public class ModKeyMapping {
	public static KeyMapping toggleDiving;
	public static KeyMapping openConfig;
	public static KeyMapping previewFilter;
	public static void register() {
		toggleDiving = register("toggleDiving", Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN);
		openConfig = register("openConfig", Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN);
		previewFilter = register("previewFilter", Type.SCANCODE, GLFW.GLFW_KEY_UNKNOWN);
	}
	@SuppressWarnings("SameParameterValue")
	private static KeyMapping register(String name, Type type, int key) {
		var id = CreateCyberGoggles.ID;
		return KeyBindingHelper.registerKeyBinding(new KeyMapping("key." + id + "." + name, type, key, "key.categories." + id));
	}
}
