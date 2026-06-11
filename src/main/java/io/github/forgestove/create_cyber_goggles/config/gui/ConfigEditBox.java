package io.github.forgestove.create_cyber_goggles.config.gui;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.function.Predicate;
/**
 * 自定义EditBox，选中后点击任意其他位置可取消聚焦。
 * 父Screen需在mouseClicked中调用 {@link #clearFocusIfNotHovered(double, double)}。
 * 支持旧版 {@link #setFilter(Predicate)} 输入过滤。
 */
public class ConfigEditBox extends EditBox {
	@Nullable private static ConfigEditBox focusedInstance;
	@Nullable private Predicate<String> filter;
	public ConfigEditBox(Font font, int x, int y, int width, int height, Component message) {
		super(font, x, y, width, height, message);
	}
	/** 在父Screen的mouseClicked中调用，点击非输入框区域时取消聚焦 */
	public static void clearFocusIfNotHovered(double mouseX, double mouseY) {
		if (focusedInstance == null) return;
		if (!focusedInstance.isMouseOver(mouseX, mouseY)) focusedInstance.setFocused(false);
	}
	/**
	 * 设置输入过滤器，还原旧版 EditBox.setFilter 行为。
	 * 每次字符输入/粘贴时会用完整结果字符串测试该谓词，
	 * 返回 {@code false} 则拒绝此次输入。
	 */
	public void setFilter(@Nullable Predicate<String> filter) {
		this.filter = filter;
	}
	@Override
	public void insertText(@NonNull String input) {
		if (filter == null) {
			super.insertText(input);
			return;
		}
		var oldValue = getValue();
		super.insertText(input);
		if (!filter.test(getValue())) setValue(oldValue);
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
