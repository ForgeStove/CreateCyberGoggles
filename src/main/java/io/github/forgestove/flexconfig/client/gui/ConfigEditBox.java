package io.github.forgestove.flexconfig.client.gui;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
/**
 * 自定义EditBox，选中后点击任意其他位置可取消聚焦。
 * 全局鼠标点击由加载器侧的事件订阅转发进来（见 {@code ConfigEvents}）。
 */
public class ConfigEditBox extends EditBox {
	@Nullable private static ConfigEditBox focusedInstance;
	public ConfigEditBox(Font font, int x, int y, int width, int height, Component message) {
		super(font, x, y, width, height, message);
	}
	public static void onScreenMouseClicked(double mouseX, double mouseY) {
		if (focusedInstance == null) return;
		if (!focusedInstance.isMouseOver(mouseX, mouseY)) focusedInstance.setFocused(false);
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
