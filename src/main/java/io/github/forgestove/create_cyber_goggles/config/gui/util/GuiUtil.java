package io.github.forgestove.create_cyber_goggles.config.gui.util;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
public class GuiUtil {
	public static Component styleAsState(Component component, boolean hasError, boolean hasChanged) {
		var result = component.copy();
		if (hasError) result.withStyle(ChatFormatting.RED);
		else if (hasChanged) result.withStyle(ChatFormatting.YELLOW);
		if (hasChanged) result.withStyle(ChatFormatting.ITALIC);
		return result;
	}
}
