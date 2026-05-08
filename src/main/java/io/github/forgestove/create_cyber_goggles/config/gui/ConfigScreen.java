package io.github.forgestove.create_cyber_goggles.config.gui;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.create_cyber_goggles.config.Translation;
import io.github.forgestove.create_cyber_goggles.config.tree.*;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.*;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Consumer;
public final class ConfigScreen<C> extends Screen {
	/** 按配置屏幕类型缓存最后选择的标签索引 */
	private static final Map<String, Integer> lastSelectedTabCache = new HashMap<>();
	private final RootConfigNode<C> root;
	private final String modId;
	private final C config;
	private final Consumer<C> onSave;
	private final Screen previous;
	private final HeaderAndFooterLayout layout;
	private final TabManager tabManager;
	private final String cacheKey;
	private final CategoryConfigNode<C> keybindCategory;
	private TabNavigationBar tabNavigationBar;
	private List<ConfigCategoryTab<C>> tabs;
	private Button quitButton;
	private Button saveAndQuitButton;
	public ConfigScreen(Screen previous, RootConfigNode<C> root, C config, Consumer<C> onSave, String modId) {
		super(root.getTitle());
		this.root = root;
		this.modId = modId;
		this.config = config;
		this.onSave = onSave;
		this.previous = previous;
		layout = new HeaderAndFooterLayout(this, 33, 33);
		tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
		tabs = List.of();
		cacheKey = root.getTitle().getString();
		keybindCategory = createKeybindCategory();
	}
	@Override
	protected void init() {
		root.resetToActive(config);
		keybindCategory.resetToActive(config);
		var tabNavigationBarBuilder = TabNavigationBar.builder(tabManager, width);
		tabs = new ArrayList<>();
		for (var category : root.getCategories()) {
			var tab = new ConfigCategoryTab<>(this, category, config, modId);
			tabNavigationBarBuilder.addTabs(tab);
			tabs.add(tab);
		}
		var keybindTab = new ConfigCategoryTab<>(this, keybindCategory, config, modId);
		tabNavigationBarBuilder.addTabs(keybindTab);
		tabs.add(keybindTab);
		tabNavigationBar = tabNavigationBarBuilder.build();
		initTabs(tabNavigationBar);
		addRenderableWidget(tabNavigationBar);
		var footerLayout = layout.addToFooter(LinearLayout.horizontal().spacing(8));
		quitButton = footerLayout.addChild(Button.builder(getQuitLabel(), b -> onClose()).width(200).build());
		saveAndQuitButton = footerLayout.addChild(Button.builder(getSaveLabel(), b -> saveAndQuit()).width(200).build());
		saveAndQuitButton.active = !isActiveValue() && validate() == null;
		layout.visitWidgets(abstractWidget -> {
			abstractWidget.setTabOrderGroup(1);
			addRenderableWidget(abstractWidget);
		});
		int cachedTabIndex = lastSelectedTabCache.getOrDefault(cacheKey, 0);
		if (cachedTabIndex >= tabs.size()) cachedTabIndex = 0;
		tabNavigationBar.selectTab(cachedTabIndex, false);
		repositionElements();
	}
	private void initTabs(TabNavigationBar bar) {
		var i = 0;
		for (var child : bar.children())
			if (child instanceof TabButton tabButton) {
				tabs.get(i).setTabButton(tabButton);
				++i;
			}
	}
	@Override
	protected void repositionElements() {
		refresh();
		if (tabNavigationBar == null) return;
		tabNavigationBar.setWidth(width);
		tabNavigationBar.arrangeElements();
		var i = tabNavigationBar.getRectangle().bottom();
		var screenRectangle = new ScreenRectangle(0, i, width, height - layout.getFooterHeight() - i);
		tabManager.setTabArea(screenRectangle);
		layout.setHeaderHeight(i);
		layout.arrangeElements();
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		var currentTab = tabManager.getCurrentTab();
		if (currentTab instanceof ConfigCategoryTab<?> categoryTab && categoryTab.handleKeyCapture(keyCode)) return true;
		if (tabNavigationBar.keyPressed(keyCode)) {
			cacheCurrentTabIndex();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		var currentTab = tabManager.getCurrentTab();
		if (currentTab instanceof ConfigCategoryTab<?> categoryTab && categoryTab.isCapturingKeybind() && categoryTab.handleMouseCapture(
			button)) return true;
		var result = super.mouseClicked(mouseX, mouseY, button);
		cacheCurrentTabIndex();
		return result;
	}
	private void cacheCurrentTabIndex() {
		for (var i = 0; i < tabs.size(); i++) {
			if (tabManager.getCurrentTab() != tabs.get(i)) continue;
			lastSelectedTabCache.put(cacheKey, i);
			break;
		}
	}
	@Override
	public void onClose() {
		if (isActiveValue()) {
			getMinecraft().setScreen(previous);
			return;
		}
		getMinecraft().setScreen(new ConfirmScreen(
			confirmed -> getMinecraft().setScreen(confirmed ? previous : this),
			Translation.QUIT_CONFIRM_TITLE,
			Translation.QUIT_CONFIRM_WARNING,
			Translation.QUIT_CONFIRM_LABEL,
			Translation.CANCEL_LABEL
		));
	}
	public void saveAndQuit() {
		var restartRequired = root.restartRequired(config);
		root.writeEditingToConfig(config);
		keybindCategory.writeEditingToConfig(config);
		getMinecraft().options.save();
		onSave.accept(config);
		getMinecraft().setScreen(restartRequired ? new ConfirmScreen(
			confirmed -> {
				if (confirmed) getMinecraft().stop();
				else getMinecraft().setScreen(previous);
			},
			Translation.RESTART_REQUIRED_TITLE,
			Translation.RESTART_REQUIRED_LABEL,
			Translation.QUIT_GAME,
			Translation.IGNORE_RESTART_LABEL
		) : previous);
	}
	public int getHeaderHeight() {
		return layout.getHeaderHeight();
	}
	public int getFooterHeight() {
		return layout.getFooterHeight();
	}
	public void refresh() {
		tabs.forEach(ConfigCategoryTab::refresh);
		var hasEntryError = tabs.stream().anyMatch(ConfigCategoryTab::hasEntryError);
		saveAndQuitButton.active = !isActiveValue() && validate() == null && !hasEntryError;
		quitButton.setMessage(getQuitLabel());
		saveAndQuitButton.setMessage(getSaveLabel(hasEntryError));
	}
	private boolean isActiveValue() {
		return root.isActiveValue(config) && keybindCategory.isActiveValue(config);
	}
	private Component validate() {
		var rootError = root.validate(config);
		if (rootError != null) return rootError;
		return keybindCategory.validate(config);
	}
	private Component getQuitLabel() {
		return isActiveValue() ? Translation.CANCEL_LABEL : Translation.QUIT_UNSAVED_LABEL;
	}
	private Component getSaveLabel(boolean hasEntryError) {
		return validate() == null && !hasEntryError ? Translation.SAVE_LABEL : Translation.CANNOT_SAVE_LABEL;
	}
	private Component getSaveLabel() {
		return getSaveLabel(false);
	}
	private CategoryConfigNode<C> createKeybindCategory() {
		var builder = CategoryConfigNode.<C>builder().name("keybinds").title(Translation.KEYBINDS_LABEL);
		for (var key : CCGKey.values()) {
			var mapping = key.keyMapping.get();
			builder.<Key, Key>value(value -> value.type(Key.class)
				.valueType(Key.class)
				.name(key.name())
				.title(Component.translatable(mapping.getName()))
				.defaultValue(mapping.getDefaultKey())
				.valueReader(config -> key.keyMapping.get().getKey())
				.valueWriter((config, valueKey) -> key.keyMapping.get().setKey(valueKey))
				.requiresRestart(false));
		}
		return builder.build();
	}
}
