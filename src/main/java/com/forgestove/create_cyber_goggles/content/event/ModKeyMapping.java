package com.forgestove.create_cyber_goggles.content.event;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;

import static com.forgestove.create_cyber_goggles.CreateCyberGoggles.ID;
import static com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM;
import static net.neoforged.neoforge.client.settings.KeyConflictContext.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;
public class ModKeyMapping {
	public static final Lazy<KeyMapping> OPEN_CONFIG = register("openConfig", IN_GAME, GLFW_KEY_UNKNOWN);
	public static final Lazy<KeyMapping> OPEN_STOCK = register("openStock", IN_GAME, GLFW_KEY_UNKNOWN);
	public static final Lazy<KeyMapping> PREVIEW_FILTER = register("previewFilter", UNIVERSAL, GLFW_KEY_UNKNOWN);
	public static final Lazy<KeyMapping> TOGGLE_DIVING = register("toggleDiving", IN_GAME, GLFW_KEY_UNKNOWN);
	@SuppressWarnings("SameParameterValue")
	private static Lazy<KeyMapping> register(String name, KeyConflictContext context, int key) {
		return Lazy.of(() -> new KeyMapping("key." + ID + "." + name, context, KEYSYM, key, "key.categories." + ID));
	}
	public static void register(RegisterKeyMappingsEvent event) {
		event.register(OPEN_CONFIG.get());
		event.register(OPEN_STOCK.get());
		event.register(PREVIEW_FILTER.get());
		event.register(TOGGLE_DIVING.get());
	}
}
