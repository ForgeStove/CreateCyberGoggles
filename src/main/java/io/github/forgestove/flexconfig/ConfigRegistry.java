package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.client.ConfigScreenFactory;
import io.github.forgestove.flexconfig.tree.*;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/** 多配置注册表：一个模组可以按 {@link ConfigType} 注册多份配置，各自一个文件。 */
public final class ConfigRegistry {
	/** 模组 ID → 该模组全部配置。 */
	private static final Map<String, ConfigSet> SETS = new ConcurrentHashMap<>();
	/** (模组 ID, 文件名) → 配置；供按文件精确定位（锁、下发、热重载）。 */
	private static final Map<FileKey, ConfigHandler<?, ?>> BY_FILE = new ConcurrentHashMap<>();
	/** 「主」配置的优先顺序，用于保持只认 modId 的既有调用点（GUI 入口、旧锁代码）。 */
	private static final List<ConfigType> PRIMARY_ORDER = List.of(ConfigType.CLIENT, ConfigType.COMMON, ConfigType.SERVER);
	/**
	 * 注册并返回配置 POJO。同一 (模组 ID, 类型) 只会构建一次；
	 * 同类型重复注册不同配置类会抛异常。
	 */
	public static <C> C init(Class<C> clazz) {
		var annotation = requireAnnotation(clazz);
		var modId = annotation.value();
		var type = annotation.type();
		var fileName = ConfigNaming.fileName(modId, type, annotation.fileName());
		var set = SETS.computeIfAbsent(
			modId, id -> {
				initConfigScreenForClient(id);
				return new ConfigSet(id);
			}
		);
		var handler = set.handlers.computeIfAbsent(
			type, ignored -> {
				var built = ConfigHandler.builder(clazz).type(type).fileName(fileName).build();
				BY_FILE.put(new FileKey(modId, fileName), built);
				return built;
			}
		);
		var config = handler.getConfig();
		if (clazz.isInstance(config)) return clazz.cast(config);
		throw new IllegalStateException("Config handler for %s/%s returned %s, expected %s".formatted(
			modId,
			type,
			config.getClass().getName(),
			clazz.getName()
		));
	}
	/** 读取并校验配置类上的 {@link Config} 注解。 */
	public static @NotNull Config requireAnnotation(Class<?> configClass) {
		var annotation = configClass.getAnnotation(Config.class);
		if (annotation == null) throw new IllegalStateException("Config class %s is missing @Config".formatted(configClass.getName()));
		if (annotation.value().isBlank()) throw new IllegalStateException("Mod id must be provided before building ConfigHandler");
		return annotation;
	}
	public static String getModId(@NotNull Class<?> configClass) {
		return requireAnnotation(configClass).value();
	}
	private static void initConfigScreenForClient(String modId) {
		if (FMLEnvironment.dist.isDedicatedServer()) return;
		ModList.get().getModContainerById(modId).ifPresent(container -> ConfigScreenFactory.initConfigScreen(container, modId));
	}
	// ========== 按 modId（主配置）访问 ==========
	/** 获取该模组的「主」配置处理器（CLIENT → COMMON → SERVER 优先）。 */
	public static @NotNull ConfigHandler<?, ?> getHandler(String modId) {
		var set = SETS.get(modId);
		if (set == null) throw new IllegalStateException("Config handler for id '%s' is not initialized.".formatted(modId));
		return set.primary();
	}
	/** 获取该模组的全部配置处理器，按「主配置」优先顺序排列（CLIENT → COMMON → SERVER → STARTUP）。 */
	public static List<ConfigHandler<?, ?>> getHandlers(String modId) {
		var set = SETS.get(modId);
		if (set == null) return List.of();
		var handlers = new ArrayList<ConfigHandler<?, ?>>(set.handlers.size());
		for (var type : PRIMARY_ORDER) {
			var handler = set.handlers.get(type);
			if (handler != null) handlers.add(handler);
		}
		return List.copyOf(handlers);
	}
	// ========== 按文件精确定位 ==========
	public static @NotNull ConfigHandler<?, ?> getHandler(FileKey key) {
		var handler = BY_FILE.get(key);
		if (handler == null) throw new IllegalStateException("Config handler for '%s' is not initialized.".formatted(key));
		return handler;
	}
	public static Object getActiveConfig(FileKey key) {
		return BY_FILE.containsKey(key) ? getHandler(key).getConfig() : null;
	}
	public static RootConfigNode<Object, Object> getRootConfigNode(FileKey key) {
		return getHandler(key).cast().getConfigTree();
	}
	public static void resetToSaved(FileKey key) {
		getHandler(key).cast().reloadAndCopy();
	}
	/** 将锁定值应用到活动配置（运行时）。不会修改 savedConfig，因此TOML文件不会被污染。 */
	public static void applyLockedValue(FileKey key, ValueConfigNode<Object, Object> node, Object parsed) {
		node.setActiveValue(getHandler(key).cast().getConfig(), parsed);
	}
	/** 让所有 WORLD 型延迟生效的改动真正生效（退出世界、服务端停止时调用）。 */
	public static void applyWorldRestart() {
		BY_FILE.values().forEach(ConfigHandler::applyWorldRestart);
	}
	// ========== 生命周期 ==========
	/** 世界即将开始：解析并加载全部 SERVER 型配置（存档优先，回退全局 config）。 */
	public static void loadServerConfigs(MinecraftServer server) {
		BY_FILE.values().forEach(handler -> {
			if (handler.getType() == ConfigType.SERVER) handler.loadFromDisk(server);
		});
	}
	/** 世界已停止：卸载全部 SERVER 型配置。 */
	public static void unloadServerConfigs() {
		BY_FILE.values().forEach(handler -> {
			if (handler.getType() == ConfigType.SERVER) handler.unload();
		});
	}
	// ========== 查询 ==========
	/**
	 * 该配置是否支持服务端锁定。
	 * <p>
	 * 锁的语义是「服务端强制客户端的本地配置」，只对客户端自己读的那份（CLIENT / COMMON / STARTUP）成立；
	 * {@link ConfigType#SERVER} 本就由服务端权威下发，再叠加锁会多出一份真相源。</p>
	 */
	public static boolean isLockable(FileKey key) {
		var handler = BY_FILE.get(key);
		return handler != null && handler.getType() != ConfigType.SERVER;
	}
	/** 获取所有已注册的 (模组 ID, 文件名)。 */
	public static Set<FileKey> getRegisteredFileKeys() {
		return Set.copyOf(BY_FILE.keySet());
	}
	private static final class ConfigSet {
		private final String modId;
		private final Map<ConfigType, ConfigHandler<?, ?>> handlers = new EnumMap<>(ConfigType.class);
		private ConfigSet(String modId) {
			this.modId = modId;
		}
		private ConfigHandler<?, ?> primary() {
			for (var type : PRIMARY_ORDER) {
				var handler = handlers.get(type);
				if (handler != null) return handler;
			}
			throw new IllegalStateException("Config handler for id '%s' is not initialized.".formatted(modId));
		}
	}
}
