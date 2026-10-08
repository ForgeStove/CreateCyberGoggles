package io.github.forgestove.flexconfig.client.gui;
import io.github.forgestove.flexconfig.ConfigHandler;
import io.github.forgestove.flexconfig.client.ClientUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.*;

import java.util.List;
/**
 * 一个模组有多份配置文件时的入口界面：列出全部配置文件，选中的那一份才进入 {@link ConfigScreen}。
 * <p>
 * 只有一份配置的模组不会走到这里（{@code ConfigScreenFactory} 直接打开它），所以单文件模组的观感不受影响。
 */
public final class ConfigFileScreen extends Screen {
	private static final int BUTTON_WIDTH = 200;
	private static final int BUTTON_HEIGHT = 20;
	private static final int SPACING = 4;
	private final String modId;
	private final List<ConfigHandler<?, ?>> handlers;
	@Nullable private final Screen parent;
	public ConfigFileScreen(String modId, List<ConfigHandler<?, ?>> handlers) {
		super(title(modId));
		this.modId = modId;
		this.handlers = handlers;
		parent = ClientUtil.mc.screen;
	}
	private static Component title(String modId) {
		return ModList.get()
			.getModContainerById(modId)
			.map(container -> Component.literal(container.getModInfo().getDisplayName()))
			.orElse(Component.literal(modId));
	}
	@Override
	protected void init() {
		var x = (width - BUTTON_WIDTH) / 2;
		var totalHeight = handlers.size() * BUTTON_HEIGHT + (handlers.size() - 1) * SPACING;
		var y = (height - totalHeight) / 2;
		for (var handler : handlers) {
			addRenderableWidget(Button.builder(label(handler), button -> open(handler))
				.bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
				.tooltip(Tooltip.create(Component.literal(handler.getFileName())))
				.build());
			y += BUTTON_HEIGHT + SPACING;
		}
	}
	/** 有翻译就用翻译，否则退回文件名。 */
	private Component label(ConfigHandler<?, ?> handler) {
		var key = "%s.config.file.%s".formatted(modId, handler.getType().extension());
		return Component.translatableWithFallback(key, handler.getFileName());
	}
	private void open(ConfigHandler<?, ?> handler) {
		ClientUtil.mc.setScreen(new ConfigScreen<>(handler));
	}
	@Override
	public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		super.render(gui, mouseX, mouseY, delta);
		gui.drawCenteredString(font, title, width / 2, height / 2 - 60, 0xFFFFFF);
	}
	@Override
	public void onClose() {
		ClientUtil.mc.setScreen(parent);
	}
}
