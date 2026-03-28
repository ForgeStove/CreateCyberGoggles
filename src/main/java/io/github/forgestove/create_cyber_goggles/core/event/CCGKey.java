package io.github.forgestove.create_cyber_goggles.core.event;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.KeyMappingAccessor;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import static com.mojang.blaze3d.platform.InputConstants.*;
import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public enum CCGKey {
	clickPenetrate(Type.KEYSYM, KEY_LCONTROL),
	interactOpposite(Type.KEYSYM, KEY_TAB),
	openConfig,
	openStock,
	previewFilter,
	showStress(Type.KEYSYM, KEY_TAB),
	showSuperGlue,
	stockRequestSelectAll(Type.KEYSYM, KEY_LALT),
	stockRequestSetter(Type.MOUSE, MOUSE_BUTTON_MIDDLE),
	toggleDiving,
	toggleGoggle,
	toggleItemOverlay(Type.KEYSYM, KEY_LCONTROL),
	useSchematic;
	public final KeyMapping keyMapping;
	CCGKey() {
		this(UNKNOWN);
	}
	CCGKey(@NotNull Type type, int key) {
		this(type.getOrCreate(key));
	}
	CCGKey(@NotNull Key key) {
		keyMapping = new KeyMapping(CCG.ID + ".key." + name(), key.getType(), key.getValue(), "key.categories." + CCG.ID);
	}
	public static void register() {
		for (var key : values()) KeyBindingHelper.registerKeyBinding(key.keyMapping);
	}
	public static @NotNull Component getFancyName(@NotNull KeyMapping keyMapping) {
		var accessor = (KeyMappingAccessor) keyMapping;
		return accessor.getKey().getDisplayName().copy().withStyle(keyMapping.isDown() ? ChatFormatting.GREEN : ChatFormatting.GRAY);
	}
	public boolean isDown() {
		var key = getKey();
		if (key.equals(UNKNOWN)) return false;
		var window = mc.getWindow().getWindow();
		return switch (key.getType()) {
			case MOUSE -> GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
			case KEYSYM, SCANCODE -> isKeyDown(window, key.getValue());
		};
	}
	public @NotNull Key getKey() {
		var accessor = (KeyMappingAccessor) keyMapping;
		return accessor.getKey();
	}
	public @NotNull Component getFancyName() {
		return getFancyName(keyMapping);
	}
}
