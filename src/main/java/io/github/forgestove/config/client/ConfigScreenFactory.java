package io.github.forgestove.config.client;
import io.github.forgestove.config.ConfigRegistry;
import io.github.forgestove.config.client.gui.ConfigScreen;
import net.minecraft.client.gui.screens.Screen;
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
	public static @NotNull Screen createConfigScreen(String modId) {
		return new ConfigScreen<>(ConfigRegistry.getHandler(modId));
	}
}
