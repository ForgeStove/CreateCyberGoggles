package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.*;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
public enum CCGKeyMapping {
	openConfig(GLFW.GLFW_KEY_UNKNOWN),
	openStock(GLFW.GLFW_KEY_UNKNOWN),
	previewFilter(GLFW.GLFW_KEY_UNKNOWN),
	toggleDiving(GLFW.GLFW_KEY_UNKNOWN),
	showStress(GLFW.GLFW_KEY_TAB);
	private final Lazy<KeyMapping> keyMapping;
	CCGKeyMapping(int key) {
		var id = CreateCyberGoggles.ID;
		keyMapping = Lazy.of(() -> new KeyMapping(
			"key." + id + "." + name(),
			KeyConflictContext.UNIVERSAL,
			Type.KEYSYM,
			key,
			"key.categories." + id
		));
	}
	public static void register(RegisterKeyMappingsEvent event) {
		for (var key : values()) event.register(key.get());
	}
	public @NotNull KeyMapping get() {
		return keyMapping.get();
	}
	public boolean isDown() {
		var value = get().getKey().getValue();
		if (value == GLFW.GLFW_KEY_UNKNOWN) return false;
		return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), value) == GLFW.GLFW_PRESS;
	}
}
