package io.github.forgestove.create_cyber_goggles.config;
import io.github.forgestove.create_cyber_goggles.config.annotation.Config;
import io.github.forgestove.create_cyber_goggles.config.client.ConfigScreenFactory;
import io.github.forgestove.create_cyber_goggles.config.tree.*;
import org.jetbrains.annotations.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public final class ConfigRegistry {
	private static final Map<String, ConfigHandler<?, ?>> HANDLERS = new ConcurrentHashMap<>();
	@Nullable private static String CURRENT_MOD_ID;
	public static <C> C init(Class<C> configClass) {
		var id = getModId(configClass);
		if (CURRENT_MOD_ID == null) CURRENT_MOD_ID = id;
		var handler = HANDLERS.computeIfAbsent(id, string -> ConfigHandler.builder(configClass).build());
		var config = handler.getConfig();
		if (configClass.isInstance(config)) return configClass.cast(config);
		throw new IllegalStateException("ConfigHandler returned config of type %s, expected %s".formatted(
			config.getClass().getName(),
			configClass.getName()
		));
	}
	public static String getModId(@NotNull Class<?> configClass) {
		var modId = configClass.getAnnotation(Config.class).value();
		if (modId == null || modId.isBlank()) throw new IllegalStateException("Mod id must be provided before building ConfigHandler");
		return modId;
	}
	public static String getModId() {
		if (CURRENT_MOD_ID == null) throw new IllegalStateException("No mod ID set. Call init() first.");
		return CURRENT_MOD_ID;
	}
	/** 将锁定值应用到活动配置（运行时）。不会修改 savedConfig，因此TOML文件不会被污染。 */
	public static void applyLockedValue(String modId, ValueConfigNode<Object, Object> node, Object parsed) {
		var handler = HANDLERS.get(modId);
		if (handler == null) return;
		node.setActiveValue(handler.getConfig(), parsed);
	}
	/** 从TOML重新加载并复制到两个配置，完全撤销任何锁定修改。 */
	public static void resetToSaved(String modId) {
		var handler = HANDLERS.get(modId).cast();
		var fresh = handler.load();
		handler.getConfigTree().copy(fresh, handler.getSavedConfig());
		handler.getConfigTree().copy(fresh, handler.getConfig());
	}
	/** 获取指定模组ID的活动配置POJO。 */
	public static Object getActiveConfig(String modId) {
		var handler = HANDLERS.get(modId);
		return handler != null ? handler.getConfig() : null;
	}
	/** 获取指定模组ID的根配置节点树。 */
	public static RootConfigNode<Object, Object> getRootConfigNode(String modId) {
		var handler = HANDLERS.get(modId).cast();
		return handler.getConfigTree();
	}
	/** 由仅客户端的 {@link ConfigScreenFactory} 用于创建配置界面。 */
	public static @NotNull ConfigHandler<?, ?> getHandler(String modId) {
		var handler = HANDLERS.get(modId);
		if (handler == null) throw new IllegalStateException("Config handler for id '%s' is not initialized.".formatted(modId));
		return handler;
	}
}
