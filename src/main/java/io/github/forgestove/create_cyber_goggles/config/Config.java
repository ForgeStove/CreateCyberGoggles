package io.github.forgestove.create_cyber_goggles.config;
import io.github.forgestove.create_cyber_goggles.config.annotation.ConfigClass;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
public final class Config {
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
	public static void initConfigScreen(ModContainer container, String id) {
		Supplier<IConfigScreenFactory> extension = () -> (modContainer, screen) -> createConfigScreen(id);
		container.registerExtensionPoint(IConfigScreenFactory.class, extension);
	}
	public static Screen createConfigScreen(String id) {
		var handler = HANDLERS.get(id);
		if (handler != null) return handler.createConfigScreen();
		throw new IllegalStateException("Config handler for id '" + id + "' is not initialized. Call getConfig(...) first.");
	}
	private static ConfigHandler<?> initializeIfNeeded(Class<?> configClass, Logger logger) {
		var id = configClass.getAnnotation(ConfigClass.class).value();
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
}
