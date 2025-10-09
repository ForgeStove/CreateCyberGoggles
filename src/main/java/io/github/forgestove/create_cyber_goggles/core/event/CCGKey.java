package io.github.forgestove.create_cyber_goggles.core.event;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.KeyMappingAccessor;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.*;

import static com.mojang.blaze3d.platform.InputConstants.*;
public enum CCGKey {
	openConfig,
	openStock,
	previewFilter,
	showStress(KEY_TAB),
	clickPenetrate(KEY_LCONTROL),
	toggleDiving,
	toggleGoggle;
	public final KeyMapping keyMapping;
	private boolean wasDown = false;
	private long pressStartTime;
	CCGKey() {
		this(UNKNOWN.getValue());
	}
	CCGKey(int key) {
		keyMapping = new KeyMapping(CCG.ID + ".key." + name(), key, "key.categories." + CCG.ID);
	}
	public static void register() {
		for (var key : values()) KeyBindingHelper.registerKeyBinding(key.keyMapping);
	}
	//	public boolean isDown() {
	//		var key = ((KeyMappingAccessor) keyMapping).getKey();
	//		return !key.equals(UNKNOWN) && isKeyDown(mc.getWindow().getWindow(), key.getValue());
	//	}
	public boolean isDown() {
		var key = ((KeyMappingAccessor) keyMapping).getKey();
		var isDown = key != UNKNOWN && isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key.getValue());
		var currentTime = System.currentTimeMillis();
		if (isDown && !wasDown) {
			wasDown = true;
			pressStartTime = currentTime;
			return true;
		} else if (isDown) return currentTime - pressStartTime >= 500;
		else wasDown = false;
		return false;
	}
}
