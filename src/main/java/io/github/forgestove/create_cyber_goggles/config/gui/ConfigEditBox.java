package io.github.forgestove.create_cyber_goggles.config.gui;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
/**
 * 自定义EditBox，选中后点击任意其他位置可取消聚焦。
 * 父Screen需在mouseClicked中调用 {@link #clearFocusIfNotHovered(double, double)}。
 */
public class ConfigEditBox extends EditBox {
	@Nullable private static ConfigEditBox focusedInstance;
	public ConfigEditBox(Font font, int x, int y, int width, int height, Component message) {
		super(font, x, y, width, height, message);
	}
	/** 在父Screen的mouseClicked中调用，点击非输入框区域时取消聚焦 */
	public static void clearFocusIfNotHovered(double mouseX, double mouseY) {
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
