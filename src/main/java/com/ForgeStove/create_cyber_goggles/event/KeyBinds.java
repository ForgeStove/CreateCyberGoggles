package com.ForgeStove.create_cyber_goggles.event;
import com.ForgeStove.create_cyber_goggles.CreateCyberGoggles;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.*;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

import java.util.function.BiConsumer;
@SuppressWarnings("unused") public enum KeyBinds {
	FILTER_MENU("filterMenu", GLFW.GLFW_KEY_LEFT_ALT, "Focus Filter Menu");
	private final String description;
	private final String translation;
	private final int key;
	private final boolean modifiable;
	private KeyMapping keyMapping;
	KeyBinds(String description, int defaultKey, String translation) {
		this.description = CreateCyberGoggles.MOD_ID + ".keyInfo." + description;
		this.key = defaultKey;
		this.modifiable = !description.isEmpty();
		this.translation = translation;
	}
	public static void provideLang(BiConsumer<String, String> consumer) {
		for (KeyBinds keyBinds : values())
			if (keyBinds.modifiable) consumer.accept(keyBinds.description, keyBinds.translation);
	}
	public static void register(RegisterKeyMappingsEvent event) {
		for (KeyBinds keyBinds : values()) {
			keyBinds.keyMapping = new KeyMapping(keyBinds.description, keyBinds.key, CreateCyberGoggles.NAME);
			if (!keyBinds.modifiable) continue;
			event.register(keyBinds.keyMapping);
		}
	}
	public static boolean isMouseButtonDown(int button) {
		return GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), button) == 1;
	}
	public KeyMapping getKeyMapping() {
		return keyMapping;
	}
	public boolean isPressed() {
		if (!modifiable) return isKeyDown(key);
		return keyMapping.isDown();
	}
	public static boolean isKeyDown(int key) {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key);
	}
	public String getBoundKey() {
		return keyMapping.getTranslatedKeyMessage().getString().toUpperCase();
	}
	public int getBoundCode() {
		return keyMapping.getKey().getValue();
	}
}
