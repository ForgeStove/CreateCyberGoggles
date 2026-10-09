package io.github.forgestove.flexconfig;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.api.ConfigType;
import io.github.forgestove.flexconfig.platform.Platform;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.*;
/** 配置文件名与路径的唯一来源，含旧命名 {@code <modId>.toml} 到 {@code <modId>-<type>.toml} 的自动迁移。 */
public final class ConfigNaming {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String SERVER_CONFIG_DIR = "serverconfig";
	/** 推导文件名：显式名优先，否则 {@code <modId>-<type>.toml}。 */
	public static String fileName(String modId, ConfigType type, @Nullable String explicit) {
		if (explicit != null && !explicit.isBlank()) return explicit.endsWith(".toml") ? explicit : explicit + ".toml";
		return "%s-%s.toml".formatted(modId, type.extension());
	}
	/** 全局配置目录（{@code config/}）下的完整路径。 */
	public static Path configDirPath(String fileName) {
		return Platform.get().configDir().resolve(fileName);
	}
	/**
	 * 解析该配置的读取路径。
	 * <p>
	 * SERVER 型在存档的 {@code serverconfig/} 下解析（存在则用它，否则回退全局 {@code config/}），不做迁移；
	 * 其余类型在全局 {@code config/} 下解析，并把旧命名 {@code <modId>.toml} 迁移为新命名。
	 */
	public static Path resolve(String modId, ConfigType type, String fileName, @Nullable MinecraftServer server) {
		if (type == ConfigType.SERVER && server != null) {
			var worldFile = server.getWorldPath(new LevelResource(SERVER_CONFIG_DIR + "/" + fileName));
			return Files.exists(worldFile) ? worldFile : configDirPath(fileName);
		}
		var target = configDirPath(fileName);
		migrateLegacy(modId, target);
		return target;
	}
	/** 旧命名 {@code <modId>.toml} 迁移到新命名（同目录移动，保留内容与快捷键段）；目标已存在则不动。 */
	private static void migrateLegacy(String modId, Path target) {
		if (Files.exists(target)) return;
		var legacy = configDirPath(modId + ".toml");
		if (legacy.equals(target) || !Files.exists(legacy)) return;
		try {
			Files.move(legacy, target);
			LOGGER.info("Migrated config file {} to {}", legacy.getFileName(), target.getFileName());
		} catch (IOException e) {
			LOGGER.warn("Failed to migrate config file {} to {}", legacy, target, e);
		}
	}
}
