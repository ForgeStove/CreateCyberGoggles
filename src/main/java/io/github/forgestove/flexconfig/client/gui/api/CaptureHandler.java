package io.github.forgestove.flexconfig.client.gui.api;
/**
 * 正在捕获按键输入的条目，由 Screen 转发键盘/鼠标事件。
 */
public interface CaptureHandler {
	boolean handleCaptureKey(int keyCode);
	boolean handleCaptureMouse(int button);
}
