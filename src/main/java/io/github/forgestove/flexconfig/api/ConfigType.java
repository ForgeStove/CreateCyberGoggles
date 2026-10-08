package io.github.forgestove.flexconfig.api;
import java.util.Locale;
public enum ConfigType {
	/**
	 * 客户端与服务端都需要加载的通用配置。
	 * 双端加载。
	 * 存放于全局配置目录。
	 * 不同步。
	 * 默认文件名为 {@code <modId>-common.toml}。
	 */
	COMMON,
	/**
	 * 只影响客户端状态的配置，例如画面选项。
	 * 只在客户端加载。
	 * 存放于全局配置目录。
	 * 不同步。
	 * 默认文件名为 {@code <modId>-client.toml}。
	 */
	CLIENT,
	/**
	 * 与服务器实例绑定的配置。
	 * 只在服务器启动时加载。
	 * 存放于存档专属的 {@code serverconfig} 目录，未提供时回退到全局配置目录。
	 * 连接时下发给客户端。
	 * 默认文件名为 {@code <modId>-server.toml}。
	 */
	SERVER;
	public String extension() {
		return name().toLowerCase(Locale.ROOT);
	}
}
