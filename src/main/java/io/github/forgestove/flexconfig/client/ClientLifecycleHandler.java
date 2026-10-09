package io.github.forgestove.flexconfig.client;
import io.github.forgestove.flexconfig.ConfigRegistry;
import io.github.forgestove.flexconfig.ConfigWatchService;
import io.github.forgestove.flexconfig.FileKey;
import io.github.forgestove.flexconfig.api.ConfigType;
import io.github.forgestove.flexconfig.client.gui.ConfigEditBox;
import io.github.forgestove.flexconfig.client.gui.ConfigScreen;

import java.util.*;
/**
 * 客户端侧的生命周期处理：tick、断开连接、屏幕点击。
 * <p>
 * 调用点由加载器侧的事件订阅转发过来（见 {@code ConfigEvents}），本类不含任何加载器 API。
 */
public final class ClientLifecycleHandler {
	private static final Map<String, Boolean> PREVIOUS_STATES = new HashMap<>();
	/** 热重载轮询计数。 */
	private static int watchTicks;
	/** 每客户端 tick 调用一次：快捷键触发 + 非 SERVER 型配置的外部改动热重载。 */
	public static void tick() {
		watchConfigFiles();
		if (ClientUtil.mc.screen != null) return;
		var window = ClientUtil.mc.getWindow().getWindow();
		// 快捷键可能挂在模组的任意一份配置上，按文件遍历
		ConfigRegistry.getRegisteredFileKeys().forEach(key -> ConfigRegistry.getHandler(key).tickTriggerKeybinds(window, PREVIOUS_STATES));
	}
	/** 非 SERVER 型配置的外部改动热重载（SERVER 型归服务端 tick 处理）。 */
	private static void watchConfigFiles() {
		if (++watchTicks % ConfigWatchService.INTERVAL_TICKS != 0) return;
		// 界面正在编辑的那份跳过（免得冲掉未保存的编辑），其余文件照常热重载
		var editing = ClientUtil.mc.screen instanceof ConfigScreen<?, ?> screen ? screen.editedFiles() : Set.<FileKey>of();
		for (var key : ConfigWatchService.poll(editing, ConfigType.CLIENT, ConfigType.COMMON)) {
			ConfigRegistry.getHandler(key).reloadAndCopy();
			ClientLockManager.reapply(key);
		}
	}
	/** 断开连接：丢弃服务端下发的配置、恢复锁定前的值，并让 WORLD 型的改动生效。 */
	public static void onLoggingOut() {
		ClientServerConfigCache.clear();
		ClientLockManager.clear();
		ConfigRegistry.applyWorldRestart();
	}
	/** 屏幕上的任意鼠标点击：让失去焦点的输入框取消聚焦。 */
	public static void onScreenMousePressed(double mouseX, double mouseY) {
		ConfigEditBox.onScreenMouseClicked(mouseX, mouseY);
	}
}
