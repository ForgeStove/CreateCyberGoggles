package io.github.forgestove.create_cyber_goggles;
import io.github.forgestove.flexconfig.api.ConfigChangeHandler;

import static io.github.forgestove.create_cyber_goggles.util.CCGUtil.*;
public class SoundReloadHandler implements ConfigChangeHandler {
	@Override
	public void onChange(Object oldValue, Object newValue) {
		if (isServer()) return;
		mc.getSoundManager().reload();
	}
}
