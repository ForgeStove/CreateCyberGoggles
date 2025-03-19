package com.ForgeStove.create_cyber_goggles.event;
import com.ForgeStove.create_cyber_goggles.CreateCyberGoggles;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.InputEvent.Key;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
public enum KeyBinds {
	PREVIEW_FILTER("previewFilter", GLFW.GLFW_KEY_LEFT_ALT);
	private final String description;
	private final int key;
	private final boolean modifiable;
	private KeyMapping keyMapping;
	KeyBinds(String description, int defaultKey) {
		this.description = CreateCyberGoggles.ID + ".keyInfo." + description;
		this.key = defaultKey;
		this.modifiable = !description.isEmpty();
	}
	public static void register(RegisterKeyMappingsEvent event) {
		for (KeyBinds keyBinds : values()) {
			keyBinds.keyMapping = new KeyMapping(keyBinds.description, keyBinds.key, CreateCyberGoggles.NAME);
			if (!keyBinds.modifiable) continue;
			event.register(keyBinds.keyMapping);
		}
	}
	public static boolean isKeyDown(@NotNull Key event, @NotNull KeyBinds keyBinds) {
		return event.getKey() == keyBinds.keyMapping.getKey().getValue() && event.getAction() == GLFW.GLFW_PRESS;
	}
}
