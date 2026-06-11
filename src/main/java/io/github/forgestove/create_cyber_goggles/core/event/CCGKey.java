package io.github.forgestove.create_cyber_goggles.core.event;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.KeyMappingAccessor;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import static com.mojang.blaze3d.platform.InputConstants.*;
import static io.github.forgestove.create_cyber_goggles.CCG.ID;
import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
import static org.lwjgl.glfw.GLFW.*;
public enum CCGKey {
	clickPenetrate(Type.KEYSYM, KEY_LCONTROL),
	clipboardPageScroll(Type.KEYSYM, KEY_LCONTROL),
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
	private boolean wasDown = false;
	private long pressStartTime;
	CCGKey() {
		keyMapping = new KeyMapping(ID + ".key." + name(), UNKNOWN.getValue(), CCG.CATEGORY);
	}
	CCGKey(@NotNull Type type, int key) {
		keyMapping = new KeyMapping(ID + ".key." + name(), type, key, CCG.CATEGORY);
	}
	public static void register() {
		for (var key : values()) KeyMappingHelper.registerKeyMapping(key.keyMapping);
	}
	public static @NotNull Component getFancyName(@NotNull KeyMapping keyMapping) {
		var key = ((KeyMappingAccessor) keyMapping).getKey();
		return key.getDisplayName().copy().withStyle(keyMapping.isDown() ? ChatFormatting.GREEN : ChatFormatting.GRAY);
	}
	public boolean isDown() {
		var key = getKey();
		var isDown = key != UNKNOWN && switch (key.getType()) {
			case MOUSE -> glfwGetMouseButton(mc.getWindow().handle(), key.getValue()) == GLFW_PRESS;
			case KEYSYM, SCANCODE -> isKeyDown(mc.getWindow(), key.getValue());
		};
		var currentTime = System.currentTimeMillis();
		if (isDown && !wasDown) {
			wasDown = true;
			pressStartTime = currentTime;
			return true;
		}
		if (isDown) return currentTime - pressStartTime >= 500;
		wasDown = false;
		return false;
	}
	public @NotNull Key getKey() {
		return ((KeyMappingAccessor) keyMapping).getKey();
	}
	public @NotNull Component getFancyName() {
		return getFancyName(keyMapping);
	}
}
