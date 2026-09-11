package io.github.forgestove.create_cyber_goggles.core.config;
import io.github.forgestove.flexconfig.api.ConfigChangeHandler;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public final class SoundReloadHandler implements ConfigChangeHandler {
	@Override
	public void onChange(Object oldValue, Object newValue) {
		if (isServer()) return;
		mc.getSoundManager().reload();
	}
}
