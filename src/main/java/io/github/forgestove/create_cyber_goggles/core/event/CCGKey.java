package io.github.forgestove.create_cyber_goggles.core.event;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.jarjar.nio.util.Lazy;
import org.jetbrains.annotations.NotNull;

import static com.mojang.blaze3d.platform.InputConstants.*;
import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public enum CCGKey {
	clickPenetrate(KEY_LCONTROL),
	interactOpposite(KEY_TAB),
	openConfig,
	openStock,
	previewFilter,
	showStress(KEY_TAB),
	showSuperGlue,
	stockRequestModifier(KEY_LALT),
	toggleDiving,
	toggleGoggle,
	toggleItemOverlay(KEY_LCONTROL),
	useSchematic;
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
	public static @NotNull Component getFancyName(@NotNull KeyMapping keyMapping) {
		return keyMapping.getKey().getDisplayName().copy().withStyle(keyMapping.isDown() ? ChatFormatting.GREEN : ChatFormatting.GRAY);
	}
	public boolean isDown() {
		var key = getKey();
		return !key.equals(UNKNOWN) && isKeyDown(mc.getWindow().getWindow(), key.getValue());
	}
	public @NotNull Key getKey() {
		return keyMapping.get().getKey();
	}
	public @NotNull Component getFancyName() {
		return getFancyName(keyMapping.get());
	}
}
