package io.github.forgestove.create_cyber_goggles.config;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public final class Config {
	private static final Map<String, ConfigHandler<?>> HANDLERS = new ConcurrentHashMap<>();
	private static final Map<String, Class<?>> CONFIG_TYPES = new ConcurrentHashMap<>();
	public static <T> T getConfig(Class<T> configClass, String id, Logger logger) {
		var handler = initializeIfNeeded(configClass, id, logger);
		var config = handler.getConfig();
		if (configClass.isInstance(config)) return configClass.cast(config);
		throw new IllegalStateException("ConfigHandler returned config of type "
			+ config.getClass().getName()
			+ ", expected "
			+ configClass.getName());
	}
	public static void initConfigScreen(ModContainer container, String id) {
		container.registerExtensionPoint(
			ConfigScreenFactory.class,
			() -> new ConfigScreenFactory((client, parent) -> createConfigScreen(id, parent))
		);
	}
	public static Screen createConfigScreen(String id, Screen parent) {
		var handler = HANDLERS.get(id);
		if (handler != null) return handler.createConfigScreen(parent);
		throw new IllegalStateException("Config handler for id '" + id + "' is not initialized. Call getConfig(...) first.");
	}
	private static <T> ConfigHandler<?> initializeIfNeeded(Class<T> configClass, String id, Logger logger) {
		CONFIG_TYPES.compute(
			id, (key, existingClass) -> {
				if (existingClass == null || existingClass.equals(configClass)) return configClass;
				throw new IllegalStateException("Config for id '"
					+ id
					+ "' is already initialized with "
					+ existingClass.getName()
					+ ", cannot reinitialize with "
					+ configClass.getName());
			}
		);
		return HANDLERS.computeIfAbsent(
			id,
			key -> ConfigHandler.builder(configClass)
				.id(id)
				.path(() -> FMLPaths.CONFIGDIR.get().resolve(id + ".toml"))
				.translationPrefix(id + ".config")
				.translator(key1 -> I18n.exists(key1) ? I18n.get(key1) : null)
				.logger(logger)
				.build()
		);
	}
}
