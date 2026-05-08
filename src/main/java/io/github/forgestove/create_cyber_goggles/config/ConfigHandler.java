package io.github.forgestove.create_cyber_goggles.config;
import io.github.forgestove.create_cyber_goggles.config.gui.screen.ConfigScreen;
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
public final class ConfigHandler<T> {
	private final Class<T> configClass;
	private final ConfigSerializer<T> serializer;
	private final Logger logger;
	private final String id;
	private final RootConfigNode<T> configTree;
	private final T activeConfig;
	private final T savedConfig;
	private ConfigHandler(Builder<T> builder) {
		configClass = builder.configClass;
		serializer = builder.serializerBuilder.build();
		logger = builder.logger;
		id = builder.id;
		configTree = RootConfigNode.create(newInstance(), builder.id);
		savedConfig = load();
		activeConfig = newInstance();
		configTree.copy(savedConfig, activeConfig);
	}
	public static <T> Builder<T> builder(Class<T> configClass) {
		return new Builder<>(configClass);
	}
	public T getConfig() {
		return activeConfig;
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
		return new ConfigScreen<>(parent, configTree, savedConfig, this::save, id);
	}
	private T newInstance() {
		try {
			return configClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Failed to create config instance", e);
		}
	}
	public static final class Builder<T> {
		private final Class<T> configClass;
		private final ConfigSerializer.Builder<T> serializerBuilder;
		private String id;
		private Logger logger;
		private Builder(Class<T> configClass) {
			this.configClass = configClass;
			serializerBuilder = ConfigSerializer.builder(configClass);
		}
		public Builder<T> id(String id) {
			this.id = id;
			return this;
		}
		public Builder<T> path(Supplier<Path> configPath) {
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
		public Builder<T> logger(Logger logger) {
			this.logger = logger;
			return this;
		}
		public ConfigHandler<T> build() {
			if (id == null || id.isBlank()) throw new IllegalStateException("id must be provided before building ConfigHandler");
			return new ConfigHandler<>(this);
		}
	}
}
