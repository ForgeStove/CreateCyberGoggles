package io.github.forgestove.config.client.gui;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.config.ConfigHandler;
import io.github.forgestove.config.client.*;
import io.github.forgestove.config.client.gui.api.CaptureHandler;
import io.github.forgestove.config.client.gui.entry.ConfigEntry;
import io.github.forgestove.config.tree.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.*;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.*;
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
		for (var category : root.getCategories()) {
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
			if (!allMapping.getCategory().equals("key.categories." + root.modId)) continue;
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
			.requiresRestart(false)));
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
		return validate() == null && !hasEntryError ? Translation.SAVE_LABEL : Translation.CANNOT_SAVE_LABEL;
	}
	public void saveAndQuit() {
		var restartRequired = root.restartRequired(config);
		root.writeEditingToConfig(config);
		if (keybindCategory != null) keybindCategory.writeEditingToConfig(config);
		var mc = ClientUtil.mc;
		mc.options.save();
		handler.save(config);
		ClientLockManager.flushPendingLocks(root.modId);
		handler.saveTriggerKeybinds();
		savedTriggerKeybinds = new LinkedHashMap<>(handler.getAllTriggerKeybinds());
		mc.setScreen(restartRequired ? new ConfirmScreen(
			confirmed -> {
				if (confirmed) mc.stop();
				else mc.setScreen(parent);
			},
			Translation.RESTART_REQUIRED_TITLE,
			Translation.RESTART_REQUIRED_LABEL,
			Translation.QUIT_GAME,
			Translation.IGNORE_RESTART_LABEL
		) : parent);
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
		saveButton.active = !isActiveValue() && validate() == null && !hasEntryError;
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
		handler.save(config);
	}
	public ConfigHandler<C, V> getHandler() {
		return handler;
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
