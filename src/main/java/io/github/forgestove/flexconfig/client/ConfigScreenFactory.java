package io.github.forgestove.flexconfig.client;
import io.github.forgestove.flexconfig.ConfigRegistry;
import io.github.forgestove.flexconfig.client.gui.ConfigFileScreen;
import io.github.forgestove.flexconfig.client.gui.ConfigScreen;
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
	/**
	 * 该模组的配置入口。
	 * 只有一份配置时直接打开它（与以前完全一致）；多份时先进入 {@link ConfigFileScreen} 选择。
	 */
	@Contract("_ -> new")
	public static @NotNull Screen createConfigScreen(String modId) {
		var handlers = ConfigRegistry.getHandlers(modId);
		return handlers.size() <= 1 ? new ConfigScreen<>(ConfigRegistry.getHandler(modId)) : new ConfigFileScreen(modId, handlers);
	}
}
