package io.github.forgestove.create_cyber_goggles.compat.jei;
import net.minecraft.client.gui.screens.Screen;
/** 让菜单能反向引用它关联的 Screen */
public interface ScreenReferenced {
	Screen ccg$getScreenReference();
	void ccg$setScreenReference(Screen screen);
}
