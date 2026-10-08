package io.github.forgestove.flexconfig;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.client.ConfigScreenFactory;
import io.github.forgestove.flexconfig.client.gui.factory.EntryKeybind;
import io.github.forgestove.flexconfig.tree.RootConfigNode;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.*;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.*;
/**
 * 通用配置处理器，管理单个配置文件的加载、保存和 GUI 创建。
 * <p>
 * 文件路径由 {@link ConfigNaming} 解析：除 {@link ConfigType#SERVER} 需进世界后才解析（存档目录）外，
 * 其余类型在构造期即读盘。
 *
 * @param <C> 配置类类型
 */
public final class ConfigHandler<C, V> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Map<String, Runnable> triggerActions = new LinkedHashMap<>();
	private final Class<C> configClass;
	private final RootConfigNode<C, V> configTree;
	private final ConfigSerializer<C> serializer;
	private final C savedConfig;
	private final C activeConfig;
	private final String modId;
	private final ConfigType type;
	private final String fileName;
	/** 已解析的磁盘路径；SERVER 型在进世界前为 {@code null}。 */
	@Nullable private Path resolvedPath;
	// ---- 全局快捷键系统 ----
	private final Map<String, String> triggerKeybinds = new LinkedHashMap<>();
	private ConfigHandler(Builder<C, V> builder) {
		configClass = builder.configClass;
		modId = ConfigRegistry.getModId(configClass);
		type = builder.type;
		fileName = builder.fileName;
		serializer = builder.serializerBuilder.build();
		configTree = RootConfigNode.create(newInstance(), modId, fileName, type);
		savedConfig = newInstance();
		activeConfig = newInstance();
		if (loadsAtRegistration()) loadFromDisk(null);
	}
	/**
	 * 注册期是否立即读盘。
	 * <p>
	 * 划分对齐官方（{@code CommonModLoader}）：SERVER 要进世界才知道存档目录；CLIENT 只在客户端加载；
	 * COMMON 双端都加载。区别在于官方把 COMMON/CLIENT 放在更晚的加载任务里，而这里统一提前到注册期——
	 * 这是刻意的，避免静态初始化或极早期读取拿到默认值。
	 */
	private boolean loadsAtRegistration() {
		return switch (type) {
			case SERVER -> false;
			case CLIENT -> FMLEnvironment.dist.isClient();
			case COMMON -> true;
		};
	}
	private @NotNull C newInstance() {
		try {
			return configClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Failed to create config instance", e);
		}
	}
	/** 解析路径并读盘；SERVER 型需传入服务器以定位存档目录，其余类型忽略该参数。 */
	public void loadFromDisk(@Nullable MinecraftServer server) {
		resolvedPath = ConfigNaming.resolve(modId, type, fileName, server);
		serializer.setTargetPath(resolvedPath);
		reloadAndCopy();
	}
	/** 从已解析的路径重读，并把值与快捷键复制到 saved 与 active，撤销一切运行时改动（含锁值与应用过的下发值）。 */
	public void reloadAndCopy() {
		if (resolvedPath == null) return;
		CommentedConfig toml;
		try {
			toml = serializer.read();
		} catch (SerializationException e) {
			LOGGER.error("Failed to read configuration, keeping current values", e);
			return;
		}
		var fresh = toml == null ? newInstance() : deserialize(toml);
		configTree.copy(fresh, savedConfig);
		// 运行时按需同步：待生效的延迟项保持旧值（重载不该让它们提前生效）
		configTree.copyForRuntime(fresh, activeConfig);
		triggerKeybinds.clear();
		if (toml != null) triggerKeybinds.putAll(ConfigSerializer.readKeybinds(toml));
		// 刚读过盘，把修改时间记为基线，免得热重载把自己的这次读取当成外部改动
		ConfigWatchService.markCurrent(new FileKey(modId, fileName));
	}
	/** 用服务端下发的整份内容覆盖 saved 与 active（只改内存，不落盘、不读本地文件）。 */
	public void applySynced(CommentedConfig toml) {
		var fresh = deserialize(toml);
		configTree.copy(fresh, savedConfig);
		configTree.copy(fresh, activeConfig);
	}
	/** 卸载（SERVER 型在世界停止时调用）：恢复默认值并丢弃已解析路径。 */
	public void unload() {
		resolvedPath = null;
		var defaults = newInstance();
		configTree.copy(defaults, savedConfig);
		configTree.copy(defaults, activeConfig);
		triggerKeybinds.clear();
	}
	private C deserialize(CommentedConfig toml) {
		try {
			return serializer.deserializeFrom(toml);
		} catch (SerializationException e) {
			LOGGER.error("Failed to load configuration, using defaults", e);
			return newInstance();
		}
	}
	@Contract(value = "_ -> new", pure = true)
	public static <C, V> @NotNull Builder<C, V> builder(Class<C> configClass) {
		return new Builder<>(configClass);
	}
	@Contract(pure = true)
	public RootConfigNode<C, V> getConfigTree() {
		return configTree;
	}
	@Contract(pure = true)
	public C getConfig() {
		return activeConfig;
	}
	/** 由 {@link ConfigScreenFactory} 用于访问已保存的配置实例。 */
	@Contract(pure = true)
	public C getSavedConfig() {
		return savedConfig;
	}
	/**
	 * 获取配置序列化器，用于网络包等场景的类型转换和反序列化。
	 */
	@Contract(pure = true)
	public ConfigSerializer<C> getSerializer() {
		return serializer;
	}
	@Contract(pure = true)
	public String getModId() {
		return modId;
	}
	@Contract(pure = true)
	public ConfigType getType() {
		return type;
	}
	@Contract(pure = true)
	public String getFileName() {
		return fileName;
	}
	/** 已解析的磁盘路径；SERVER 型在进世界前为 {@code null}。 */
	@Contract(pure = true)
	public @Nullable Path getPath() {
		return resolvedPath;
	}
	@Contract(value = " -> this", pure = true)
	@SuppressWarnings("unchecked")
	public ConfigHandler<Object, Object> cast() {
		return (ConfigHandler<Object, Object>) this;
	}
	// ========== 全局快捷键系统 ==========
	/** 保存：值、本地化注释与快捷键段一次性原子写出（临时文件 + 原子替换）。 */
	public void save(C config) {
		// 全量进文件模型 → 补上延迟生效项的新值（运行时仍守旧值）→ 运行时只接收即时生效的改动
		configTree.copy(config, savedConfig);
		configTree.flushPending(savedConfig);
		configTree.copyForRuntime(config, activeConfig);
		if (resolvedPath == null) return;   // 路径未解析（SERVER 未进世界）时没有落盘目标
		try {
			serializer.serialize(savedConfig, triggerKeybinds);
		} catch (SerializationException e) {
			LOGGER.error("Failed to save configuration", e);
		}
		// 刚写过盘，把修改时间记为基线，免得热重载把自己的这次写入当成外部改动
		ConfigWatchService.markCurrent(new FileKey(modId, fileName));
	}
	/** 让 WORLD 型延迟生效的改动真正生效（退出世界、服务端停止时调用）。 */
	public void applyWorldRestart() {
		configTree.applyRestart(savedConfig, activeConfig, RestartType.WORLD);
	}
	/** 设置指定路径的快捷键绑定与触发动作。serialized 为空时清除绑定；action 为 null 时不覆盖已有动作。 */
	public void setTriggerKeybind(String path, @Nullable String serialized, @Nullable Runnable action) {
		if (serialized == null || serialized.isEmpty()) triggerKeybinds.remove(path);
		else triggerKeybinds.put(path, serialized);
		if (action != null) triggerActions.put(path, action);
	}
	/** 获取指定路径的快捷键序列化字符串。 */
	public @Nullable String getTriggerKeybind(String path) {
		return triggerKeybinds.get(path);
	}
	/** 获取所有快捷键绑定的只读视图。 */
	public Map<String, String> getAllTriggerKeybinds() {
		return Collections.unmodifiableMap(triggerKeybinds);
	}
	/** 遍历所有快捷键，边缘检测并执行触发动作。 */
	public void tickTriggerKeybinds(long window, Map<String, Boolean> previousStates) {
		triggerKeybinds.forEach((path, value) -> {
			var keybind = EntryKeybind.deserialize(value);
			if (keybind.isUnbound()) return;
			var isPressed = EntryKeybind.isKeyPressed(window, keybind);
			var wasPressed = previousStates.getOrDefault(path, false);
			previousStates.put(path, isPressed);
			if (isPressed && !wasPressed) {
				var action = triggerActions.get(path);
				if (action != null) action.run();
			}
		});
	}
	/** 将快捷键绑定恢复到指定的快照（用于取消时回撤清屏幕时还原）。 */
	public void restoreTriggerKeybinds(Map<String, String> snapshot) {
		triggerKeybinds.clear();
		triggerKeybinds.putAll(snapshot);
	}
	public static final class Builder<C, V> {
		private final Class<C> configClass;
		private final ConfigSerializer.Builder<C> serializerBuilder;
		private ConfigType type = ConfigType.CLIENT;
		private String fileName;
		@Contract(pure = true)
		private Builder(Class<C> configClass) {
			this.configClass = configClass;
			serializerBuilder = ConfigSerializer.builder(configClass);
		}
		@Contract(value = "_ -> this", pure = true)
		public Builder<C, V> type(ConfigType type) {
			this.type = type;
			return this;
		}
		@Contract(value = "_ -> this", pure = true)
		public Builder<C, V> fileName(String fileName) {
			this.fileName = fileName;
			return this;
		}
		@Contract(" -> new")
		public @NotNull ConfigHandler<C, V> build() {
			return new ConfigHandler<>(this);
		}
	}
}
