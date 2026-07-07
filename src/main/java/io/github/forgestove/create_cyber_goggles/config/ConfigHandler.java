package io.github.forgestove.create_cyber_goggles.config;
import com.mojang.logging.LogUtils;
import io.github.forgestove.create_cyber_goggles.config.annotation.Config;
import io.github.forgestove.create_cyber_goggles.config.client.ConfigScreenFactory;
import io.github.forgestove.create_cyber_goggles.config.tree.RootConfigNode;
import org.jetbrains.annotations.*;
import org.slf4j.Logger;
/**
 * 通用配置处理器，管理配置的加载、保存和GUI创建。
 *
 * @param <C> 配置类类型
 */
public final class ConfigHandler<C, V> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Class<C> configClass;
	private final RootConfigNode<C, V> configTree;
	private final ConfigSerializer<C> serializer;
	private final C savedConfig;
	private final C activeConfig;
	private ConfigHandler(Builder<C, V> builder) {
		configClass = builder.configClass;
		serializer = builder.serializerBuilder.build();
		configTree = RootConfigNode.create(newInstance(), configClass.getAnnotation(Config.class).value());
		savedConfig = load();
		activeConfig = newInstance();
		configTree.copy(savedConfig, activeConfig);
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
	public void save(C config) {
		configTree.copy(config, savedConfig);
		configTree.copy(config, activeConfig);
		try {
			serializer.serialize(config);
		} catch (SerializationException e) {
			LOGGER.error("Failed to save configuration", e);
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
