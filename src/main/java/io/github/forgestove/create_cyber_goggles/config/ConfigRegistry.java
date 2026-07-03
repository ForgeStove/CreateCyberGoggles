package io.github.forgestove.create_cyber_goggles.config;
import io.github.forgestove.create_cyber_goggles.config.annotation.Config;
import io.github.forgestove.create_cyber_goggles.config.client.ConfigScreenFactory;
import io.github.forgestove.create_cyber_goggles.config.tree.*;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public final class ConfigRegistry {
	private static final Map<String, ConfigHandler<?>> HANDLERS = new ConcurrentHashMap<>();
	private static final Map<String, Class<?>> CONFIG_TYPES = new ConcurrentHashMap<>();
	public static <C> C getConfig(Class<C> configClass, Logger logger) {
		var handler = initializeIfNeeded(configClass, logger);
		var config = handler.getConfig();
		if (configClass.isInstance(config)) return configClass.cast(config);
		throw new IllegalStateException("ConfigHandler returned config of type %s, expected %s".formatted(
			config.getClass().getName(),
			configClass.getName()
		));
	}
	private static ConfigHandler<?> initializeIfNeeded(Class<?> configClass, Logger logger) {
		var id = configClass.getAnnotation(Config.class).value();
		if (id == null || id.isBlank()) throw new IllegalStateException("Mod id must be provided before building ConfigHandler");
		CONFIG_TYPES.compute(
			id, (key, existingClass) -> {
				if (existingClass == null || existingClass.equals(configClass)) return configClass;
				throw new IllegalStateException("Config for id '%s' is already initialized with %s, cannot reinitialize with %s".formatted(id,
					existingClass.getName(),
					configClass.getName()
				));
			}
		);
		return HANDLERS.computeIfAbsent(
			id,
			string -> ConfigHandler.builder(configClass)
				.path(() -> FMLPaths.CONFIGDIR.get().resolve(id + ".toml"))
				.translationPrefix(id + ".config")
				.translator(key -> I18n.exists(key) ? I18n.get(key) : null)
				.logger(logger)
				.build()
		);
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
	public static ConfigHandler<?> getHandler(String id) {
		return HANDLERS.get(id);
	}
}
