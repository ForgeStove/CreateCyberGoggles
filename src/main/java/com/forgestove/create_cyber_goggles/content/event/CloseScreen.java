package com.forgestove.create_cyber_goggles.content.event;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ScreenEvent.Closing;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import org.jetbrains.annotations.NotNull;
public class CloseScreen {
	public static void onCloseScreen(@NotNull Closing event) {
		if (!(event.getScreen() instanceof ConfigurationScreen)) return;
		var mc = Minecraft.getInstance();
		mc.tell(() -> {if (mc.screen == null) mc.mouseHandler.grabMouse();});
	}
}
