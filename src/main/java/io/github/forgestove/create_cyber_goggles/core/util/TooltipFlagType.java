package io.github.forgestove.create_cyber_goggles.core.util;
import net.minecraft.world.item.TooltipFlag.Default;
import org.jetbrains.annotations.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@SuppressWarnings("unused")
public enum TooltipFlagType {
	Default,
	Normal(false),
	Advanced(true);
	private final @Nullable Default flag;
	@Contract(pure = true)
	TooltipFlagType(boolean advanced) {
		flag = new Default(advanced, false);
	}
	@Contract(pure = true)
	TooltipFlagType() {
		flag = null;
	}
	@Contract(pure = true)
	public Default getFlag() {
		return flag == null ? new Default(mc.options.advancedItemTooltips, false) : flag;
	}
}
