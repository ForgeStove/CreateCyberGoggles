package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.InputEvent.Key;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
public enum KeyBind {
	OPEN_CONFIG("openConfig", GLFW.GLFW_KEY_UNKNOWN),
	OPEN_STOCK("openStock", GLFW.GLFW_KEY_UNKNOWN),
	PREVIEW_FILTER("previewFilter", GLFW.GLFW_KEY_UNKNOWN),
	TOGGLE_DIVING("toggleDiving", GLFW.GLFW_KEY_UNKNOWN);
	private final String description;
	private final int key;
	private final boolean modifiable;
	private KeyMapping keyMapping;
	KeyBind(String description, int defaultKey) {
		this.description = "key.%s.%s".formatted(CreateCyberGoggles.ID, description);
		this.key = defaultKey;
		this.modifiable = !description.isEmpty();
	}
	public static void register(RegisterKeyMappingsEvent event) {
		for (var keyBind : values()) {
			keyBind.keyMapping = new KeyMapping(keyBind.description, keyBind.key, CreateCyberGoggles.NAME);
			if (!keyBind.modifiable) continue;
			event.register(keyBind.keyMapping);
		}
	}
	public static boolean isAction(@NotNull Key event, @NotNull KeyBind keyBind, int press) {
		return event.getKey() == keyBind.keyMapping.getKey().getValue() && event.getAction() == press;
	}
}
