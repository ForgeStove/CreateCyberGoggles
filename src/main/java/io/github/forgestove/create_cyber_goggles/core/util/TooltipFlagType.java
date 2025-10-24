package io.github.forgestove.create_cyber_goggles.core.util;
import net.minecraft.world.item.TooltipFlag.Default;
import org.jetbrains.annotations.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@SuppressWarnings("unused")
public enum TooltipFlagType {
	Default(mc.options.advancedItemTooltips),
	Normal(false),
	Advanced(true);
	private final Default flag;
	@Contract(pure = true)
	TooltipFlagType(boolean flag) {
		this.flag = new Default(flag, false);
	}
	@Contract(pure = true)
	public Default getFlag() {
		return this == Default ? new Default(mc.options.advancedItemTooltips, false) : flag;
	}
	public @NotNull String toString() {
		return CCGLang.translate("enum." + name()).string();
	}
}
