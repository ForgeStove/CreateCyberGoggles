package io.github.forgestove.create_cyber_goggles.config;
import io.github.forgestove.create_cyber_goggles.config.gui.ConfigScreen;
import io.github.forgestove.create_cyber_goggles.config.tree.RootConfigNode;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.function.*;
/**
 * Generic configuration handler that manages config loading, saving, and GUI creation.
 *
 * @param <T> Configuration class type
 */
@SuppressWarnings("unused")
public final class ConfigHandler<T> {
	private final Class<T> configClass;
	private final ConfigSerializer<T> serializer;
	private final Logger logger;
	private final RootConfigNode<T> configTree;
	private final T activeConfig;
	private final T savedConfig;
	private ConfigHandler(Builder<T> builder) {
		this.configClass = builder.configClass;
		this.serializer = builder.serializerBuilder.build();
		this.logger = builder.logger;
		this.configTree = RootConfigNode.create(newInstance());
		this.savedConfig = load();
		this.activeConfig = newInstance();
		this.configTree.copy(savedConfig, activeConfig);
	}
	public static <T> Builder<T> builder(Class<T> configClass) {
		return new Builder<>(configClass);
	}
	public T getConfig() {
		return activeConfig;
	}
	public T getSavedConfig() {
		return savedConfig;
	}
	public RootConfigNode<T> getConfigTree() {
		return configTree;
	}
	public void save(T config) {
		configTree.copy(config, savedConfig);
		configTree.copy(config, activeConfig);
		try {
			serializer.serialize(config);
		} catch (SerializationException e) {
			if (logger != null) logger.error("Failed to save configuration", e);
		}
	}
	/**
	 * Regenerates the config file with proper translations.
	 * Call this after I18n is fully loaded (e.g., when player joins world or opens config screen).
	 */
	public void regenerateConfigFile() {
		save(savedConfig);
	}
	public T load() {
		try {
			return serializer.deserialize();
		} catch (SerializationException e) {
			if (logger != null) logger.error("Failed to load configuration, using defaults", e);
			return newInstance();
		}
	}
	public Screen createConfigScreen(Screen parent) {
		// Regenerate config file with translations now that I18n is loaded
		regenerateConfigFile();
		return new ConfigScreen<>(parent, configTree, savedConfig, this::save);
	}
	public Consumer<T> getSaveConsumer() {
		return this::save;
	}
	private T newInstance() {
		try {
			return configClass.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to create config instance", e);
		}
	}
	public static final class Builder<T> {
		private final Class<T> configClass;
		private final ConfigSerializer.Builder<T> serializerBuilder;
		private Logger logger;
		private Builder(Class<T> configClass) {
			this.configClass = configClass;
			this.serializerBuilder = ConfigSerializer.builder(configClass);
		}
		public Builder<T> path(Supplier<Path> configPath) {
			serializerBuilder.path(configPath);
			return this;
		}
		public Builder<T> path(Path configPath) {
			serializerBuilder.path(configPath);
			return this;
		}
		public Builder<T> translationPrefix(String prefix) {
			serializerBuilder.translationPrefix(prefix);
			return this;
		}
		public Builder<T> translator(Function<String, String> translator) {
			serializerBuilder.translator(translator);
			return this;
		}
		public Builder<T> header(Supplier<String> headerSupplier) {
			serializerBuilder.header(headerSupplier);
			return this;
		}
		public Builder<T> logger(Logger logger) {
			this.logger = logger;
			return this;
		}
		public ConfigHandler<T> build() {
			return new ConfigHandler<>(this);
		}
	}
}


