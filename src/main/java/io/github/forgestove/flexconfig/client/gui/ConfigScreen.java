package io.github.forgestove.flexconfig.client.gui;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.flexconfig.ConfigHandler;
import io.github.forgestove.flexconfig.FileKey;
import io.github.forgestove.flexconfig.api.ConfigType;
import io.github.forgestove.flexconfig.api.RestartType;
import io.github.forgestove.flexconfig.client.*;
import io.github.forgestove.flexconfig.client.gui.api.CaptureHandler;
import io.github.forgestove.flexconfig.client.gui.entry.ConfigEntry;
import io.github.forgestove.flexconfig.tree.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.*;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.util.*;
public final class ConfigScreen<C, V> extends Screen {
	public static CategoryConfigNode<?> lastSelectedCategory;
	public final RootConfigNode<C, V> root;
	private final C config;
	private final ConfigHandler<C, V> handler;
	private final Screen parent;
	private final TabManager tabManager;
	private final List<ConfigCategoryTab<C, V>> tabs;
	private final HeaderAndFooterLayout layout;
	private CategoryConfigNode<C> keybindCategory;
	private TabNavigationBar tabNavigationBar;
	private Button cancelButton, saveButton;
	private CaptureHandler capturingEntry;
	private Map<String, String> savedTriggerKeybinds = Map.of();
	public ConfigScreen(ConfigHandler<C, V> handler) {
		super(handler.getConfigTree().getTitle());
		root = handler.getConfigTree();
		config = handler.getConfig();
		this.handler = handler;
		parent = ClientUtil.mc.screen;
		tabs = new ArrayList<>();
		layout = new HeaderAndFooterLayout(this);
		tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (capturingEntry != null && capturingEntry.handleCaptureKey(keyCode)) return true;
		if (tabNavigationBar != null && tabNavigationBar.keyPressed(keyCode)) {
			cacheTabIndex();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	private void cacheTabIndex() {
		if (tabManager.getCurrentTab() instanceof ConfigCategoryTab<?, ?> tab) lastSelectedCategory = tab.node;
	}
	@Override
	protected void init() {
		capturingEntry = null;
		ClientLockManager.clearPendingLocks();
		savedTriggerKeybinds = new LinkedHashMap<>(handler.getAllTriggerKeybinds());
		tabs.clear();
		root.resetToActive(config);
		for (var category : root.categories()) {
			var tab = new ConfigCategoryTab<>(this, category, config);
			tabs.add(tab);
		}
		var keybindCat = buildKeybindCategory();
		if (keybindCat != null) {
			keybindCat.resetToActive(config);
			keybindCategory = keybindCat;
			var keybindTab = new ConfigCategoryTab<>(this, keybindCategory, config);
			tabs.add(keybindTab);
		} else keybindCategory = null;
		initTabLayout();
		var buttonWidth = ConfigEntry.SIZE * 8;
		cancelButton = Button.builder(getCancelLabel(), b -> onClose()).width(buttonWidth).build();
		saveButton = Button.builder(getSaveLabel(false), b -> saveAndQuit()).width(buttonWidth).build();
		var footerLyt = layout.addToFooter(LinearLayout.horizontal().spacing(8));
		footerLyt.addChild(cancelButton);
		footerLyt.addChild(saveButton);
		layout.visitWidgets(w -> {
			w.setTabOrderGroup(1);
			addRenderableWidget(w);
		});
		repositionElements();
	}
	private @Nullable CategoryConfigNode<C> buildKeybindCategory() {
		var allMappings = ClientUtil.mc.options.keyMappings;
		var modMappings = new ArrayList<KeyMapping>();
		for (var allMapping : allMappings) {
			if (!allMapping.getCategory().equals("key.categories." + root.modId())) continue;
			modMappings.add(allMapping);
		}
		if (modMappings.isEmpty()) return null;
		var builder = CategoryConfigNode.<C>builder().title(Translation.KEYBINDS_LABEL);
		modMappings.forEach(mapping -> builder.<Key>value(value -> value.valueType(Key.class)
			.name(mapping.getName())
			.path("keybinds." + mapping.getName())
			.title(Component.translatable(mapping.getName()))
			.defaultValue(mapping.getDefaultKey())
			.valueReader(config -> mapping.getKey())
			.valueWriter((config, valueKey) -> mapping.setKey(valueKey))
			.restartType(null)));
		return builder.build();
	}
	private void initTabLayout() {
		var builder = TabNavigationBar.builder(tabManager, width);
		tabs.forEach(builder::addTabs);
		tabNavigationBar = builder.build();
		initTabs(tabNavigationBar);
		addRenderableWidget(tabNavigationBar);
		tabNavigationBar.selectTab(lastTabIndex(), false);
	}
	private Component getCancelLabel() {
		return isActiveValue() ? Translation.CANCEL_LABEL : Translation.QUIT_UNSAVED_LABEL;
	}
	@Override
	public void onClose() {
		var mc = ClientUtil.mc;
		if (isActiveValue()) {
			mc.setScreen(parent);
			return;
		}
		mc.setScreen(new ConfirmScreen(
			confirmed -> {
				if (confirmed) handler.restoreTriggerKeybinds(savedTriggerKeybinds);
				mc.setScreen(confirmed ? parent : this);
			}, Translation.QUIT_CONFIRM_TITLE, Translation.QUIT_CONFIRM_WARNING, Translation.QUIT_CONFIRM_LABEL, Translation.CANCEL_LABEL
		));
	}
	private Component getSaveLabel(boolean hasEntryError) {
		return isEditable() && validate() == null && !hasEntryError ? Translation.SAVE_LABEL : Translation.CANNOT_SAVE_LABEL;
	}
	public void saveAndQuit() {
		if (!isEditable()) return;
		var restartType = root.restartType(config);
		root.writeEditingToConfig(config);
		if (keybindCategory != null) keybindCategory.writeEditingToConfig(config);
		var mc = ClientUtil.mc;
		mc.options.save();
		handler.save(config);
		ClientLockManager.flushPendingLocks();
		savedTriggerKeybinds = new LinkedHashMap<>(handler.getAllTriggerKeybinds());
		mc.setScreen(restartScreen(restartType));
	}
	/**
	 * 按所需的重启级别给出后续界面。
	 * 只需重载世界时提供「返回标题界面」，需要重启游戏时才提供「退出游戏」。
	 */
	private Screen restartScreen(@Nullable RestartType restartType) {
		if (restartType == null) return parent;
		var mc = ClientUtil.mc;
		if (restartType == RestartType.WORLD) {
			if (mc.level == null) return parent;   // 不在世界里，没有可重载的世界
			return new ConfirmScreen(
				confirmed -> {
					if (confirmed) mc.disconnect();
					else mc.setScreen(parent);
				},
				Translation.RESTART_REQUIRED_TITLE,
				Translation.RESTART_REQUIRED_WORLD,
				mc.isLocalServer() ? Translation.RETURN_TO_MENU : CommonComponents.GUI_DISCONNECT,
				Translation.IGNORE_RESTART_LABEL
			);
		}
		return new ConfirmScreen(
			confirmed -> {
				if (confirmed) mc.stop();
				else mc.setScreen(parent);
			},
			Translation.RESTART_REQUIRED_TITLE,
			Translation.RESTART_REQUIRED_LABEL,
			Translation.QUIT_GAME,
			Translation.IGNORE_RESTART_LABEL
		);
	}
	@Override
	protected void repositionElements() {
		refresh();
		repositionTabLayout();
	}
	private void initTabs(@NotNull TabNavigationBar bar) {
		var i = 0;
		for (var child : bar.children()) if (child instanceof TabButton tabButton) tabs.get(i++).setTabButton(tabButton);
	}
	private int lastTabIndex() {
		if (lastSelectedCategory == null) return 0;
		for (var i = 0; i < tabs.size(); i++)
			if (tabs.get(i).node == lastSelectedCategory) return i;
		return 0;
	}
	private boolean isActiveValue() {
		if (ClientLockManager.hasPendingLocks()) return false;
		if (!root.isActiveValue(config) || keybindCategory != null && !keybindCategory.isActiveValue(config)) return false;
		return savedTriggerKeybinds.equals(handler.getAllTriggerKeybinds());
	}
	private @Nullable Component validate() {
		var rootError = root.validate(config);
		if (rootError != null) return rootError;
		if (keybindCategory != null) return keybindCategory.validate(config);
		return null;
	}
	public void refresh() {
		tabs.forEach(ConfigCategoryTab::refresh);
		var hasEntryError = tabs.stream().anyMatch(ConfigCategoryTab::hasEntryError);
		saveButton.active = isEditable() && !isActiveValue() && validate() == null && !hasEntryError;
		cancelButton.setMessage(getCancelLabel());
		saveButton.setMessage(getSaveLabel(hasEntryError));
	}
	private void repositionTabLayout() {
		if (tabNavigationBar == null) return;
		tabNavigationBar.setWidth(width);
		tabNavigationBar.arrangeElements();
		var i = tabNavigationBar.getRectangle().bottom();
		var screenRectangle = new ScreenRectangle(0, i, width, height - layout.getFooterHeight() - i);
		tabManager.setTabArea(screenRectangle);
		layout.setHeaderHeight(i);
		layout.arrangeElements();
	}
	/** 由全局快捷键触发调用：将已由 entry 写入配置的值持久化到磁盘。 */
	public void saveByKeybind() {
		if (!isEditable()) return;
		handler.save(config);
	}
	public ConfigHandler<C, V> getHandler() {
		return handler;
	}
	/** 界面正在编辑的配置文件；热重载会跳过它们，免得冲掉未保存的编辑。 */
	public Set<FileKey> editedFiles() {
		return Set.of(new FileKey(root.modId(), root.fileName()));
	}
	/** 本界面所显示的配置是否支持服务端锁定（SERVER 型由服务端权威下发，不需要锁定）。 */
	public boolean isLockable() {
		return handler.getType() != ConfigType.SERVER;
	}
	/**
	 * 本界面所显示的配置是否可编辑。
	 * <p>
	 * 远端连服、或单人存档已对局域网开放时，SERVER 型由服务端权威，客户端不可改（与官方一致）。
	 * 单人未开放时客户端就是服务端，可编辑。
	 */
	public boolean isEditable() {
		if (handler.getType() != ConfigType.SERVER) return true;
		var mc = ClientUtil.mc;
		if (!mc.isSingleplayer()) return false;
		var server = mc.getSingleplayerServer();
		return server == null || !server.isPublished();
	}
	public void onEntryCaptureChanged(CaptureHandler entry, boolean capturing) {
		capturingEntry = capturing ? entry : null;
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (capturingEntry != null && capturingEntry.handleCaptureMouse(button)) return true;
		var result = super.mouseClicked(mouseX, mouseY, button);
		cacheTabIndex();
		return result;
	}
	public int getHeaderHeight() {
		return layout.getHeaderHeight();
	}
	public int getFooterHeight() {
		return layout.getFooterHeight();
	}
}
