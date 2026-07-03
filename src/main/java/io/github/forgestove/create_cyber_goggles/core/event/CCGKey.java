package io.github.forgestove.create_cyber_goggles.core.event;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGMods;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.neoforged.jarjar.nio.util.Lazy;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import static com.mojang.blaze3d.platform.InputConstants.*;
import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public enum CCGKey {
	clickPenetrate(Type.KEYSYM, KEY_LCONTROL),
	clipboardPageScroll(Type.KEYSYM, KEY_LCONTROL),
	correctionSublevel(Type.KEYSYM, KEY_LCONTROL, CCGMods.SIMULATED),
	handleMoveSublevel(Type.KEYSYM, KEY_LCONTROL, CCGMods.SIMULATED),
	interactOpposite(Type.KEYSYM, KEY_TAB),
	openConfig,
	openStock,
	previewFilter,
	showHoneyGlue(CCGMods.SIMULATED),
	showStress(Type.KEYSYM, KEY_TAB),
	showSuperGlue,
	stockRequestSelectAll(Type.KEYSYM, KEY_LALT),
	stockRequestSetter(Type.MOUSE, MOUSE_BUTTON_MIDDLE),
	toggleDiving,
	toggleGoggle,
	toggleItemOverlay(Type.KEYSYM, KEY_LCONTROL),
	usePhysicsStaff(CCGMods.SIMULATED),
	useSchematic;
	public final Lazy<KeyMapping> keyMapping;
	private final CCGMods requiredMod;
	CCGKey() {
		this(UNKNOWN, null);
	}
	CCGKey(@NotNull Key key, CCGMods requiredMod) {
		this.requiredMod = requiredMod;
		keyMapping = Lazy.of(new KeyMapping(CCG.ID + ".key." + name(), key.getType(), key.getValue(), "key.categories." + CCG.ID));
	}
	CCGKey(@NotNull CCGMods requiredMod) {
		this(UNKNOWN, requiredMod);
	}
	CCGKey(@NotNull Type type, int key) {
		this(type.getOrCreate(key), null);
	}
	CCGKey(@NotNull Type type, int key, CCGMods requiredMod) {
		this(type.getOrCreate(key), requiredMod);
	}
	public static void register(RegisterKeyMappingsEvent event) {
		for (var key : values()) if (key.shouldRegister()) event.register(key.keyMapping.get());
	}
	public boolean shouldRegister() {
		return requiredMod == null || requiredMod.isLoaded();
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
		return keyMapping.get().getKey();
	}
	public @NotNull Component getFancyName() {
		return getFancyName(keyMapping.get());
	}
	public static @NotNull Component getFancyName(@NotNull KeyMapping keyMapping) {
		return keyMapping.getKey().getDisplayName().copy().withStyle(keyMapping.isDown() ? ChatFormatting.GREEN : ChatFormatting.GRAY);
	}
}
