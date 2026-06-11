package io.github.forgestove.create_cyber_goggles.config.gui;
import io.github.forgestove.create_cyber_goggles.config.Translation;
import io.github.forgestove.create_cyber_goggles.config.tree.RootConfigNode;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.*;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Consumer;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public final class ConfigScreen<C> extends Screen {
	/** 按配置屏幕类型缓存最后选择的标签索引 */
	private static final Map<String, Integer> lastSelectedTabCache = new HashMap<>();
	public final RootConfigNode<C> root;
	private final C config;
	private final Consumer<C> onSave;
	private final Screen previous;
	private final HeaderAndFooterLayout layout;
	private final TabManager tabManager;
	private final String cacheKey;
	private TabNavigationBar tabNavigationBar;
	private List<ConfigCategoryTab<C>> tabs;
	private Button quitButton;
	private Button saveAndQuitButton;
	public ConfigScreen(RootConfigNode<C> root, C config, Consumer<C> onSave) {
		super(root.getTitle());
		this.root = root;
		this.config = config;
		this.onSave = onSave;
		previous = mc.screen;
		layout = new HeaderAndFooterLayout(this, 33, 33);
		tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
		tabs = List.of();
		cacheKey = root.getTitle().getString();
	}
	@Override
	protected void init() {
		root.resetToActive(config);
		var tabNavigationBarBuilder = TabNavigationBar.builder(tabManager, width);
		tabs = new ArrayList<>();
		for (var category : root.getCategories()) {
			var tab = new ConfigCategoryTab<>(this, category, config);
			tabNavigationBarBuilder.addTabs(tab);
			tabs.add(tab);
		}
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
	public boolean keyPressed(@NonNull KeyEvent keyEvent) {
		if (tabNavigationBar.keyPressed(keyEvent)) {
			cacheCurrentTabIndex();
			return true;
		}
		return super.keyPressed(keyEvent);
	}
	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
		ConfigEditBox.clearFocusIfNotHovered(event.x(), event.y());
		var result = super.mouseClicked(event, bl);
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
			minecraft.setScreen(previous);
			return;
		}
		minecraft.setScreen(new ConfirmScreen(
			confirmed -> minecraft.setScreen(confirmed ? previous : this),
			Translation.QUIT_CONFIRM_TITLE,
			Translation.QUIT_CONFIRM_WARNING,
			Translation.QUIT_CONFIRM_LABEL,
			Translation.CANCEL_LABEL
		));
	}
	public void saveAndQuit() {
		var restartRequired = root.restartRequired(config);
		root.writeEditingToConfig(config);
		minecraft.options.save();
		onSave.accept(config);
		minecraft.setScreen(restartRequired ? new ConfirmScreen(
			confirmed -> {
				if (confirmed) minecraft.stop();
				else minecraft.setScreen(previous);
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
		return root.isActiveValue(config);
	}
	private Component validate() {
		return root.validate(config);
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
}
