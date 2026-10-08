package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.api.ConfigType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
/**
 * 配置文件的外部改动探测（修改时间轮询）。
 * <p>
 * 不使用 NeoForge 的 {@code FileWatcher}：那套只挂在 {@code ConfigTracker} 上，只监听经 {@code registerConfig}
 * 注册的配置，自管的 {@link ConfigHandler} 接不进去。
 * <p>
 * 调用方负责节流与按类型分工：客户端轮询非 SERVER 型，服务端轮询 SERVER 型。
 */
public final class ConfigWatchService {
	/** 轮询间隔（tick）。 */
	public static final int INTERVAL_TICKS = 20;
	/** 上次见到的修改时间；为 {@code null} 表示该文件尚未建立基线。 */
	private static final Map<FileKey, Long> STAMPS = new HashMap<>();
	/** 记录当前修改时间为基线。读写完成后调用，避免把自己的改动当成外部改动。 */
	public static void markCurrent(FileKey key) {
		var path = handlerPath(key);
		if (path == null || !Files.exists(path)) {
			STAMPS.remove(key);
			return;
		}
		try {
			STAMPS.put(key, Files.getLastModifiedTime(path).toMillis());
		} catch (IOException e) {
			STAMPS.remove(key);
		}
	}
	/**
	 * 返回自上次轮询以来被外部改动过的文件，并更新基线。
	 * 首次见到某个文件时只建立基线、不报告（否则启动即触发一次无意义的重载）。
	 * <p>
	 * {@code excluded}（例如配置界面正在编辑的文件）会被完全跳过、**不更新基线**，
	 * 这样等它们退出排除后仍能被检出，不会漏掉那次外部改动。</p>
	 */
	public static Set<FileKey> poll(Set<FileKey> excluded, ConfigType... types) {
		if (types.length == 0) return Set.of();
		var allowed = EnumSet.copyOf(List.of(types));
		var changed = new LinkedHashSet<FileKey>();
		for (var key : ConfigRegistry.getRegisteredFileKeys()) {
			if (excluded.contains(key) || !allowed.contains(ConfigRegistry.getHandler(key).getType())) continue;
			var path = handlerPath(key);
			if (path == null || !Files.exists(path)) continue;
			long stamp;
			try {
				stamp = Files.getLastModifiedTime(path).toMillis();
			} catch (IOException e) {
				continue;
			}
			var previous = STAMPS.put(key, stamp);
			if (previous != null && previous != stamp) changed.add(key);
		}
		return changed;
	}
	@Nullable
	private static Path handlerPath(FileKey key) {
		return ConfigRegistry.getRegisteredFileKeys().contains(key) ? ConfigRegistry.getHandler(key).getPath() : null;
	}
}
