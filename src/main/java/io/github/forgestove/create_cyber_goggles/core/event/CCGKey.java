package io.github.forgestove.create_cyber_goggles.core.event;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.jarjar.nio.util.Lazy;

import static com.mojang.blaze3d.platform.InputConstants.*;
import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public enum CCGKey {
	openConfig,
	openStock,
	previewFilter,
	showStress(KEY_TAB),
	clickPenetrate(KEY_LCONTROL),
	toggleDiving,
	toggleGoggle;
	public final Lazy<KeyMapping> keyMapping;
	CCGKey() {
		this(UNKNOWN.getValue());
	}
	CCGKey(int key) {
		keyMapping = Lazy.of(new KeyMapping(CCG.ID + ".key." + name(), key, "key.categories." + CCG.ID));
	}
	public static void register(RegisterKeyMappingsEvent event) {
		for (var key : values()) event.register(key.keyMapping.get());
	}
	public boolean isDown() {
		var key = keyMapping.get().getKey();
		return !key.equals(UNKNOWN) && isKeyDown(mc.getWindow().getWindow(), key.getValue());
	}
}
