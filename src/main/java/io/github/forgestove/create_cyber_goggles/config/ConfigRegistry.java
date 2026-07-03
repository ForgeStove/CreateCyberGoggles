package io.github.forgestove.create_cyber_goggles.config;
import io.github.forgestove.create_cyber_goggles.config.annotation.Config;
import io.github.forgestove.create_cyber_goggles.config.client.ConfigScreenFactory;
import io.github.forgestove.create_cyber_goggles.config.tree.*;
import org.jetbrains.annotations.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public final class ConfigRegistry {
	private static final Map<String, ConfigHandler<?>> HANDLERS = new ConcurrentHashMap<>();
	public static <C> C init(Class<C> configClass) {
		var handler = HANDLERS.computeIfAbsent(getModId(configClass), string -> ConfigHandler.builder(configClass).build());
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
	/** 获取第一个注册的配置的模组ID。供需要命名空间的配置系统类使用。 */
	public static String getModId() {
		if (HANDLERS.isEmpty()) throw new IllegalStateException("No config handler registered yet");
		return HANDLERS.keySet().iterator().next();
	}
	/** 将锁定值应用到活动配置（运行时）。不会修改 savedConfig，因此TOML文件不会被污染。 */
	@SuppressWarnings("unchecked")
	public static void applyLockedValue(String modId, ValueConfigNode<?, ?> node, Object parsed) {
		var handler = HANDLERS.get(modId);
		if (handler == null) return;
		((ValueConfigNode<Object, Object>) node).setActiveValue(handler.getConfig(), parsed);
	}
	/** 从TOML重新加载并复制到两个配置，完全撤销任何锁定修改。 */
	@SuppressWarnings({"unchecked"})
	public static <C> void resetToSaved(String modId) {
		var handler = (ConfigHandler<C>) HANDLERS.get(modId);
		if (handler == null) return;
		var fresh = handler.load();
		handler.getConfigTree().copy(fresh, handler.getSavedConfig());
		handler.getConfigTree().copy(fresh, handler.getConfig());
	}
	/** 获取指定模组ID的活动配置POJO。 */
	@Nullable
	public static Object getActiveConfig(String modId) {
		var handler = HANDLERS.get(modId);
		return handler != null ? handler.getConfig() : null;
	}
	/** 获取指定模组ID的根配置节点树。 */
	@Nullable
	public static RootConfigNode<?> getRootConfigNode(String modId) {
		var handler = HANDLERS.get(modId);
		return handler != null ? handler.getConfigTree() : null;
	}
	/** 由仅客户端的 {@link ConfigScreenFactory} 用于创建配置界面。 */
	@Nullable
	public static ConfigHandler<?> getHandler(String modId) {
		return HANDLERS.get(modId);
	}
}
