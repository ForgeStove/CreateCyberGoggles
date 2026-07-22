package io.github.forgestove.flexconfig;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.api.Config;
import io.github.forgestove.flexconfig.client.ConfigScreenFactory;
import io.github.forgestove.flexconfig.client.gui.factory.EntryKeybind;
import io.github.forgestove.flexconfig.tree.RootConfigNode;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
/**
 * 通用配置处理器，管理配置的加载、保存和 GUI 创建。
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
	private final Path configPath;
	// ---- 全局快捷键系统 ----
	private final Map<String, String> triggerKeybinds = new LinkedHashMap<>();
	private ConfigHandler(Builder<C, V> builder) {
		configClass = builder.configClass;
		serializer = builder.serializerBuilder.build();
		var modId = configClass.getAnnotation(Config.class).value();
		configPath = FMLPaths.CONFIGDIR.get().resolve(modId + ".toml");
		configTree = RootConfigNode.create(newInstance(), modId);
		savedConfig = load();
		activeConfig = newInstance();
		configTree.copy(savedConfig, activeConfig);
		loadTriggerKeybinds();
	}
	private @NotNull C newInstance() {
		try {
			return configClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Failed to create config instance", e);
		}
	}
	public C load() {
		try {
			return serializer.deserialize();
		} catch (SerializationException e) {
			LOGGER.error("Failed to load configuration, using defaults", e);
			return newInstance();
		}
	}
	private void loadTriggerKeybinds() {
		if (!configPath.toFile().exists()) return;
		try (var lines = Files.lines(configPath)) {
			var inKeybinds = new boolean[1];
			lines.forEach(line -> {
				var trimmed = line.trim();
				if (trimmed.equals("[-keybinds-]")) {
					inKeybinds[0] = true;
					return;
				}
				if (inKeybinds[0] && trimmed.startsWith("[") && trimmed.endsWith("]")) {
					inKeybinds[0] = false;
					return;
				}
				if (!inKeybinds[0] || !trimmed.contains("=")) return;
				var eqIdx = trimmed.indexOf('=');
				var key = trimmed.substring(0, eqIdx).trim();
				var value = trimmed.substring(eqIdx + 1).trim();
				if (value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length() - 1);
				if (value.startsWith("'") && value.endsWith("'")) value = value.substring(1, value.length() - 1);
				if (!key.isEmpty()) triggerKeybinds.put(key, value);
			});
		} catch (IOException e) {
			LOGGER.error("Failed to load trigger keybinds", e);
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
	@Contract(value = " -> this", pure = true)
	@SuppressWarnings("unchecked")
	public ConfigHandler<Object, Object> cast() {
		return (ConfigHandler<Object, Object>) this;
	}
	// ========== 全局快捷键系统 ==========
	public void save(C config) {
		configTree.copy(config, savedConfig);
		configTree.copy(config, activeConfig);
		try {
			serializer.serialize(config);
		} catch (SerializationException e) {
			LOGGER.error("Failed to save configuration", e);
		}
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
		for (var entry : triggerKeybinds.entrySet()) {
			var path = entry.getKey();
			var keybind = EntryKeybind.deserialize(entry.getValue());
			if (keybind.isUnbound()) continue;
			var isPressed = EntryKeybind.isKeyPressed(window, keybind);
			var wasPressed = previousStates.getOrDefault(path, false);
			previousStates.put(path, isPressed);
			if (isPressed && !wasPressed) {
				var action = triggerActions.get(path);
				if (action != null) action.run();
			}
		}
	}
	/** 将快捷键绑定恢复到指定的快照（用于取消时回撤清屏幕时还原）。 */
	public void restoreTriggerKeybinds(Map<String, String> snapshot) {
		triggerKeybinds.clear();
		triggerKeybinds.putAll(snapshot);
	}
	/** 将快捷键绑定持久化到配置文件的 {keybinds} 段（纯文本，不经过 NightConfig 重写）。 */
	public void saveTriggerKeybinds() {
		try {
			if (!configPath.toFile().exists()) return;
			var lines = Files.readAllLines(configPath);
			var out = new ArrayList<String>();
			var inKeybinds = false;
			for (var line : lines) {
				var trimmed = line.trim();
				if (trimmed.equals("[-keybinds-]")) {
					inKeybinds = true;
					continue;
				}
				if (inKeybinds && trimmed.startsWith("[") && trimmed.endsWith("]")) inKeybinds = false;
				if (!inKeybinds) out.add(line);
			}
			if (!triggerKeybinds.isEmpty()) {
				if (!out.isEmpty() && !out.getLast().isEmpty()) out.add("");
				out.add("[-keybinds-]");
				for (var entry : triggerKeybinds.entrySet()) {
					var val = entry.getValue();
					if (val == null || val.isEmpty()) continue;
					out.add("\t%s = \"%s\"".formatted(entry.getKey(), val.replace("\\", "\\\\").replace("\"", "\\\"")));
				}
			}
			Files.write(configPath, out);
		} catch (IOException e) {
			LOGGER.error("Failed to save trigger keybinds", e);
		}
	}
	public static final class Builder<C, V> {
		private final Class<C> configClass;
		private final ConfigSerializer.Builder<C> serializerBuilder;
		@Contract(pure = true)
		private Builder(Class<C> configClass) {
			this.configClass = configClass;
			serializerBuilder = ConfigSerializer.builder(configClass);
		}
		@Contract(" -> new")
		public @NotNull ConfigHandler<C, V> build() {
			return new ConfigHandler<>(this);
		}
	}
}
