package io.github.forgestove.create_cyber_goggles.config;
import io.github.forgestove.create_cyber_goggles.config.annotation.ConfigClass;
import io.github.forgestove.create_cyber_goggles.config.client.ConfigScreenFactory;
import io.github.forgestove.create_cyber_goggles.config.tree.RootConfigNode;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.function.*;
/**
 * Generic configuration handler that manages config loading, saving, and GUI creation.
 *
 * @param <C> Configuration class type
 */
public final class ConfigHandler<C> {
	private final Class<C> configClass;
	private final ConfigSerializer<C> serializer;
	private final Logger logger;
	private final RootConfigNode<C> configTree;
	private final C activeConfig;
	private final C savedConfig;
	private ConfigHandler(Builder<C> builder) {
		configClass = builder.configClass;
		serializer = builder.serializerBuilder.build();
		logger = builder.logger;
		configTree = RootConfigNode.create(newInstance(), configClass.getAnnotation(ConfigClass.class).value());
		savedConfig = load();
		activeConfig = newInstance();
		configTree.copy(savedConfig, activeConfig);
	}
	private C newInstance() {
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
			if (logger != null) logger.error("Failed to load configuration, using defaults", e);
			return newInstance();
		}
	}
	public static <C> Builder<C> builder(Class<C> configClass) {
		return new Builder<>(configClass);
	}
	public RootConfigNode<C> getConfigTree() {
		return configTree;
	}
	public C getConfig() {
		return activeConfig;
	}
	/** Used by {@link ConfigScreenFactory} to access the saved config instance. */
	public C getSavedConfig() {
		return savedConfig;
	}
	/**
	 * 获取配置序列化器，用于网络包等场景的类型转换和反序列化。
	 */
	public ConfigSerializer<C> getSerializer() {
		return serializer;
	}
	/**
	 * 重新生成配置文件并进行翻译。在I18n完全加载后（比如玩家加入世界或打开配置界面时）调用它。
	 */
	public void regenerateConfigFile() {
		try {
			serializer.serialize(savedConfig);
		} catch (SerializationException e) {
			if (logger != null) logger.error("Failed to regenerate config file", e);
		}
	}
	public void save(C config) {
		configTree.copy(config, savedConfig);
		configTree.copy(config, activeConfig);
		try {
			serializer.serialize(config);
		} catch (SerializationException e) {
			if (logger != null) logger.error("Failed to save configuration", e);
		}
	}
	public static final class Builder<C> {
		private final Class<C> configClass;
		private final ConfigSerializer.Builder<C> serializerBuilder;
		private Logger logger;
		private Builder(Class<C> configClass) {
			this.configClass = configClass;
			serializerBuilder = ConfigSerializer.builder(configClass);
		}
		public Builder<C> path(Supplier<Path> configPath) {
			serializerBuilder.path(configPath);
			return this;
		}
		public Builder<C> translationPrefix(String prefix) {
			serializerBuilder.translationPrefix(prefix);
			return this;
		}
		public Builder<C> translator(Function<String, String> translator) {
			serializerBuilder.translator(translator);
			return this;
		}
		public Builder<C> logger(Logger logger) {
			this.logger = logger;
			return this;
		}
		public ConfigHandler<C> build() {
			return new ConfigHandler<>(this);
		}
	}
}
