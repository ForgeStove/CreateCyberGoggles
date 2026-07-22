package io.github.forgestove.flexconfig.client;
import io.github.forgestove.flexconfig.ConfigRegistry;
import io.github.forgestove.flexconfig.client.gui.ConfigScreen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.*;

import java.util.function.Supplier;
public final class ConfigScreenFactory {
	public static void initConfigScreen(@NotNull ModContainer container, String id) {
		Supplier<IConfigScreenFactory> extension = () -> (modContainer, screen) -> createConfigScreen(id);
		container.registerExtensionPoint(IConfigScreenFactory.class, extension);
	}
	@Contract("_ -> new")
	public static @NotNull ConfigScreen<?, ?> createConfigScreen(String modId) {
		return new ConfigScreen<>(ConfigRegistry.getHandler(modId));
	}
}
