package io.github.forgestove.create_cyber_goggles.config.client;
import io.github.forgestove.create_cyber_goggles.config.*;
import io.github.forgestove.create_cyber_goggles.config.client.gui.ConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.function.Supplier;
/**
 * 仅客户端辅助，用于注册配置界面工厂。
 * 与配置分离，以避免在服务器上加载客户端类。
 */
public final class ConfigScreenFactory {
	public static void initConfigScreen(ModContainer container, String id) {
		Supplier<IConfigScreenFactory> extension = () -> (modContainer, screen) -> createConfigScreen(id);
		container.registerExtensionPoint(IConfigScreenFactory.class, extension);
	}
	public static Screen createConfigScreen(String modId) {
		var handler = ConfigRegistry.getHandler(modId);
		if (handler == null) throw new IllegalStateException("Config handler for id '%s' is not initialized.".formatted(modId));
		return create(handler);
	}
	private static <C> Screen create(ConfigHandler<C> handler) {
		return new ConfigScreen<>(handler);
	}
}
