package io.github.forgestove.create_cyber_goggles.core.event;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.KeyMappingAccessor;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import static com.mojang.blaze3d.platform.InputConstants.*;
import static io.github.forgestove.create_cyber_goggles.CCG.ID;
public enum CCGKey {
	clickPenetrate(KEY_LCONTROL),
	interactOpposite(KEY_TAB),
	openConfig,
	openStock,
	previewFilter,
	showStress(KEY_TAB),
	toggleDiving,
	toggleGoggle,
	toggleItemOverlay(KEY_LCONTROL),
	useSchematic;
	public final KeyMapping keyMapping;
	private boolean wasDown = false;
	private long pressStartTime;
	CCGKey() {
		this(UNKNOWN.getValue());
	}
	CCGKey(int key) {
		keyMapping = new KeyMapping(ID + ".key." + name(), key, CCG.CATEGORY);
	}
	public static void register() {
		for (var key : values()) KeyBindingHelper.registerKeyBinding(key.keyMapping);
	}
	public static @NotNull Component getFancyName(@NotNull KeyMapping keyMapping) {
		var key = ((KeyMappingAccessor) keyMapping).getKey();
		return key.getDisplayName().copy().withStyle(keyMapping.isDown() ? ChatFormatting.GREEN : ChatFormatting.GRAY);
	}
	public boolean isDown() {
		var key = getKey();
		var isDown = key != UNKNOWN && isKeyDown(Minecraft.getInstance().getWindow(), key.getValue());
		var currentTime = System.currentTimeMillis();
		if (isDown && !wasDown) {
			wasDown = true;
			pressStartTime = currentTime;
			return true;
		} else if (isDown) return currentTime - pressStartTime >= 500;
		else wasDown = false;
		return false;
	}
	public @NotNull Key getKey() {
		return ((KeyMappingAccessor) keyMapping).getKey();
	}
	public @NotNull Component getFancyName() {
		return getFancyName(keyMapping);
	}
}
