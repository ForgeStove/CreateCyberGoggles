package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;
public class CyberKeyMapping {
	public static final Lazy<KeyMapping> OPEN_CONFIG = register("openConfig", KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_UNKNOWN);
	public static final Lazy<KeyMapping> OPEN_STOCK = register("openStock", KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_UNKNOWN);
	public static final Lazy<KeyMapping> PREVIEW_FILTER = register("previewFilter", KeyConflictContext.UNIVERSAL, GLFW.GLFW_KEY_UNKNOWN);
	public static final Lazy<KeyMapping> TOGGLE_DIVING = register("toggleDiving", KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_UNKNOWN);
	@SuppressWarnings("SameParameterValue")
	private static Lazy<KeyMapping> register(String name, KeyConflictContext context, int key) {
		var id = CreateCyberGoggles.ID;
		return Lazy.of(() -> new KeyMapping("key." + id + "." + name, context, Type.KEYSYM, key, "key.categories." + id));
	}
	public static void register(RegisterKeyMappingsEvent event) {
		event.register(OPEN_CONFIG.get());
		event.register(OPEN_STOCK.get());
		event.register(PREVIEW_FILTER.get());
		event.register(TOGGLE_DIVING.get());
	}
}
