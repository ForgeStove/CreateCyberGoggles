package io.github.forgestove.flexconfig.client.gui;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ScreenEvent.MouseButtonPressed.Pre;
import org.jetbrains.annotations.Nullable;
/**
 * 自定义EditBox，选中后点击任意其他位置可取消聚焦。
 * 通过NeoForge屏幕事件自动监听全局鼠标点击。
 */
public class ConfigEditBox extends EditBox {
	@Nullable private static ConfigEditBox focusedInstance;
	public ConfigEditBox(Font font, int x, int y, int width, int height, Component message) {
		super(font, x, y, width, height, message);
	}
	public static void onScreenMouseClicked(Pre event) {
		if (focusedInstance == null) return;
		if (!focusedInstance.isMouseOver(event.getMouseX(), event.getMouseY())) focusedInstance.setFocused(false);
	}
	@Override
	public void setFocused(boolean focused) {
		if (focused && focusedInstance != this) {
			if (focusedInstance != null) focusedInstance.setFocused(false);
			focusedInstance = this;
		} else if (!focused && focusedInstance == this) focusedInstance = null;
		super.setFocused(focused);
	}
}
