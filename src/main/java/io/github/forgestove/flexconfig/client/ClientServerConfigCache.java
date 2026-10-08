package io.github.forgestove.flexconfig.client;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.api.ConfigType;
import org.slf4j.Logger;

import java.io.StringReader;
import java.util.*;
/**
 * 保存服务端下发的 SERVER 型配置。
 * <p>
 * 只存内存：官方同样如此（下发后那份配置的 path 为 {@code null}），客户端本地的
 * {@code <modId>-server.toml} 不会因此被改写，也不参与读取。
 */
public final class ClientServerConfigCache {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<FileKey, CommentedConfig> SYNCED = new HashMap<>();
	/** 收到服务端下发的整份 SERVER 配置：覆盖 saved 与 active（不落盘）。 */
	public static void accept(String modId, String fileName, String tomlContent) {
		var key = new FileKey(modId, fileName);
		if (!isServerConfig(key)) return;
		CommentedConfig toml;
		try {
			toml = new TomlParser().parse(new StringReader(tomlContent));
		} catch (Exception e) {
			LOGGER.error("Failed to parse server config '{}'", key, e);
			return;
		}
		SYNCED.put(key, toml);
		ConfigRegistry.getHandler(key).cast().applySynced(toml);
	}
	/** 断开连接时丢弃下发内容并恢复默认值。 */
	public static void clear() {
		SYNCED.keySet().forEach(key -> ConfigRegistry.getHandler(key).unload());
		SYNCED.clear();
	}
	private static boolean isServerConfig(FileKey key) {
		return ConfigRegistry.getRegisteredFileKeys().contains(key) && ConfigRegistry.getHandler(key).getType() == ConfigType.SERVER;
	}
}
