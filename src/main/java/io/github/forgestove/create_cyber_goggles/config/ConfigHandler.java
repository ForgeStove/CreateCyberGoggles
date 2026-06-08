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
 * @param <C> Configuration class type
 */
public final class ConfigHandler<C> {
	private final Class<C> configClass;
	private final ConfigSerializer<C> serializer;
	private final Logger logger;
	private final String id;
	private final RootConfigNode<C> configTree;
	private final C activeConfig;
	private final C savedConfig;
	private ConfigHandler(Builder<C> builder) {
		configClass = builder.configClass;
		serializer = builder.serializerBuilder.build();
		logger = builder.logger;
		id = builder.id;
		configTree = RootConfigNode.create(newInstance(), builder.id);
		savedConfig = load();
		activeConfig = newInstance();
		configTree.copy(savedConfig, activeConfig);
	}
	public static <C> Builder<C> builder(Class<C> configClass) {
		return new Builder<>(configClass);
	}
	public C getConfig() {
		return activeConfig;
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
	/**
	 * Regenerates the config file with proper translations.
	 * Call this after I18n is fully loaded (e.g., when player joins world or opens config screen).
	 */
	public void regenerateConfigFile() {
		save(savedConfig);
	}
	public C load() {
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
	private C newInstance() {
		try {
			return configClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Failed to create config instance", e);
		}
	}
	public static final class Builder<C> {
		private final Class<C> configClass;
		private final ConfigSerializer.Builder<C> serializerBuilder;
		private String id;
		private Logger logger;
		private Builder(Class<C> configClass) {
			this.configClass = configClass;
			serializerBuilder = ConfigSerializer.builder(configClass);
		}
		public Builder<C> id(String id) {
			this.id = id;
			return this;
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
			if (id == null || id.isBlank()) throw new IllegalStateException("id must be provided before building ConfigHandler");
			return new ConfigHandler<>(this);
		}
	}
}
