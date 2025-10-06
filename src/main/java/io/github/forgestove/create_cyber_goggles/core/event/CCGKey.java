package io.github.forgestove.create_cyber_goggles.core.event;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.jarjar.nio.util.Lazy;
import org.lwjgl.glfw.GLFW;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public enum CCGKey {
	openConfig(GLFW.GLFW_KEY_UNKNOWN),
	openStock(GLFW.GLFW_KEY_UNKNOWN),
	previewFilter(GLFW.GLFW_KEY_UNKNOWN),
	showStress(GLFW.GLFW_KEY_TAB),
	toggleDiving(GLFW.GLFW_KEY_UNKNOWN),
	toggleGoggle(GLFW.GLFW_KEY_UNKNOWN);
	public final Lazy<KeyMapping> keyMapping;
	CCGKey(int key) {
		keyMapping = Lazy.of(new KeyMapping(CCG.ID + ".key." + name(), key, "key.categories." + CCG.ID));
	}
	public static void register(RegisterKeyMappingsEvent event) {
		for (var key : values()) event.register(key.keyMapping.get());
	}
	public boolean isDown() {
		var key = keyMapping.get().getKey();
		return key != InputConstants.UNKNOWN && InputConstants.isKeyDown(mc.getWindow().getWindow(), key.getValue());
	}
}
