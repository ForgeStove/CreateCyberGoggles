package io.github.forgestove.create_cyber_goggles.util;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.IconButtonAccessor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
public final class FilterPreviewUtil {
	public static void lock(Screen screen) {
		screen.setFocused(null);
		for (GuiEventListener listener : screen.children()) {
			if (listener instanceof IconButton button && ((IconButtonAccessor) button).getIcon() == AllIcons.I_CONFIRM) continue;
			if (listener instanceof EditBox editBox) editBox.setEditable(false);
			if (listener instanceof AbstractWidget widget) widget.active = false;
		}
	}
}
