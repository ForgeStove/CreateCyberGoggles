package io.github.forgestove.create_cyber_goggles.config.client.gui;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.create_cyber_goggles.config.client.*;
import io.github.forgestove.create_cyber_goggles.config.client.gui.api.CaptureHandler;
import io.github.forgestove.create_cyber_goggles.config.client.gui.entry.ConfigEntry;
import io.github.forgestove.create_cyber_goggles.config.tree.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.*;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Consumer;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public final class ConfigScreen<C> extends Screen {
	private static CategoryConfigNode<?> lastSelectedCategory;
	public final RootConfigNode<C> root;
	private final C config;
	private final Consumer<C> onSave;
	private final Screen parent;
	private final HeaderAndFooterLayout layout;
	private final TabManager tabManager;
	private CategoryConfigNode<C> keybindCategory;
	private TabNavigationBar tabNavigationBar;
	private List<ConfigCategoryTab<C>> tabs;
	private Button cancelButton;
	private Button saveButton;
	private CaptureHandler capturingEntry;
	public ConfigScreen(RootConfigNode<C> root, C config, Consumer<C> onSave) {
		super(root.getTitle());
		this.root = root;
		this.config = config;
		this.onSave = onSave;
		parent = mc.screen;
		layout = new HeaderAndFooterLayout(this, 33, 33);
		tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
		tabs = List.of();
		keybindCategory = null;
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (capturingEntry != null && capturingEntry.handleCaptureKey(keyCode)) return true;
		if (tabNavigationBar.keyPressed(keyCode)) {
			cacheCurrentTabIndex();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	private void cacheCurrentTabIndex() {
		if (tabManager.getCurrentTab() instanceof ConfigCategoryTab<?> tab) lastSelectedCategory = tab.getCategoryNode();
	}
	@Override
	public void onClose() {
		if (isActiveValue()) {
			getMinecraft().setScreen(parent);
			return;
		}
		getMinecraft().setScreen(new ConfirmScreen(
			confirmed -> getMinecraft().setScreen(confirmed ? parent : this),
			Translation.QUIT_CONFIRM_TITLE,
			Translation.QUIT_CONFIRM_WARNING,
			Translation.QUIT_CONFIRM_LABEL,
			Translation.CANCEL_LABEL
		));
	}
	@Override
	protected void init() {
		capturingEntry = null;
		ClientLockManager.clearPendingLocks();
		root.resetToActive(config);
		var entryTypeRegistry = new EntryTypeRegistry<C>();
		var tabNavigationBarBuilder = TabNavigationBar.builder(tabManager, width);
		tabs = new ArrayList<>();
		for (var category : root.getCategories()) {
			var tab = new ConfigCategoryTab<>(this, category, config, entryTypeRegistry);
			tabNavigationBarBuilder.addTabs(tab);
			tabs.add(tab);
		}
		var keybindCat = buildKeybindCategory();
		if (keybindCat != null) {
			keybindCat.resetToActive(config);
			keybindCategory = keybindCat;
			var keybindTab = new ConfigCategoryTab<>(this, keybindCategory, config, entryTypeRegistry);
			tabNavigationBarBuilder.addTabs(keybindTab);
			tabs.add(keybindTab);
		} else keybindCategory = null;
		tabNavigationBar = tabNavigationBarBuilder.build();
		initTabs(tabNavigationBar);
		addRenderableWidget(tabNavigationBar);
		var footerLayout = layout.addToFooter(LinearLayout.horizontal().spacing(8));
		var buttonWidth = ConfigEntry.SIZE * 8;
		cancelButton = footerLayout.addChild(Button.builder(getCancelLabel(), b -> onClose()).width(buttonWidth).build());
		saveButton = footerLayout.addChild(Button.builder(getSaveLabel(false), b -> saveAndQuit()).width(buttonWidth).build());
		layout.visitWidgets(abstractWidget -> {
			abstractWidget.setTabOrderGroup(1);
			addRenderableWidget(abstractWidget);
		});
		tabNavigationBar.selectTab(lastTabIndex(), false);
		repositionElements();
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
	private CategoryConfigNode<C> buildKeybindCategory() {
		var allMappings = getMinecraft().options.keyMappings;
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
	private void initTabs(TabNavigationBar bar) {
		var i = 0;
		for (var child : bar.children()) if (child instanceof TabButton tabButton) tabs.get(i++).setTabButton(tabButton);
	}
	private Component getCancelLabel() {
		return isActiveValue() ? Translation.CANCEL_LABEL : Translation.QUIT_UNSAVED_LABEL;
	}
	private Component getSaveLabel(boolean hasEntryError) {
		return validate() == null && !hasEntryError ? Translation.SAVE_LABEL : Translation.CANNOT_SAVE_LABEL;
	}
	public void saveAndQuit() {
		var restartRequired = root.restartRequired(config);
		root.writeEditingToConfig(config);
		if (keybindCategory != null) keybindCategory.writeEditingToConfig(config);
		getMinecraft().options.save();
		onSave.accept(config);
		ClientLockManager.flushPendingLocks(root.modId);
		getMinecraft().setScreen(restartRequired ? new ConfirmScreen(
			confirmed -> {
				if (confirmed) getMinecraft().stop();
				else getMinecraft().setScreen(parent);
			},
			Translation.RESTART_REQUIRED_TITLE,
			Translation.RESTART_REQUIRED_LABEL,
			Translation.QUIT_GAME,
			Translation.IGNORE_RESTART_LABEL
		) : parent);
	}
	private int lastTabIndex() {
		if (lastSelectedCategory == null) return 0;
		for (var i = 0; i < tabs.size(); i++)
			if (tabs.get(i).getCategoryNode() == lastSelectedCategory) return i;
		return 0;
	}
	private boolean isActiveValue() {
		if (ClientLockManager.hasPendingLocks()) return false;
		return root.isActiveValue(config) && (keybindCategory == null || keybindCategory.isActiveValue(config));
	}
	private Component validate() {
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
	public void onEntryCaptureChanged(CaptureHandler entry, boolean capturing) {
		capturingEntry = capturing ? entry : null;
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (capturingEntry != null && capturingEntry.handleCaptureMouse(button)) return true;
		var result = super.mouseClicked(mouseX, mouseY, button);
		cacheCurrentTabIndex();
		return result;
	}
	public int getHeaderHeight() {
		return layout.getHeaderHeight();
	}
	public int getFooterHeight() {
		return layout.getFooterHeight();
	}
}
