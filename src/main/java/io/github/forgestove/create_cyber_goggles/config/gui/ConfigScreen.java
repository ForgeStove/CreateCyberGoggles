package io.github.forgestove.create_cyber_goggles.config.gui;
import io.github.forgestove.create_cyber_goggles.config.tree.RootConfigNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.*;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
public final class ConfigScreen<C> extends Screen {
	/** Cache for last selected tab index per config screen type */
	private static final Map<String, Integer> lastSelectedTabCache = new HashMap<>();
	private final RootConfigNode<C> root;
	private final C config;
	private final Consumer<C> onSave;
	private final Screen previous;
	private final HeaderAndFooterLayout layout;
	private final TabManager tabManager;
	private final String cacheKey;
	private final PanoramaRenderer panoramaRenderer = new PanoramaRenderer(TitleScreen.CUBE_MAP);
	private TabNavigationBar tabNavigationBar;
	private List<ConfigCategoryTab<C>> tabs;
	private Button quitButton;
	private Button saveAndQuitButton;
	public ConfigScreen(Screen previous, RootConfigNode<C> root, C config, Consumer<C> onSave) {
		super(root.getTitle());
		this.root = root;
		this.config = config;
		this.onSave = onSave;
		this.previous = previous;
		this.layout = new HeaderAndFooterLayout(this, 24, 33);
		this.tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
		this.tabs = List.of();
		this.cacheKey = root.getTitle().getString();
	}
	@Override
	protected void init() {
		this.root.resetToActive(this.config);
		var tabNavigationBarBuilder = TabNavigationBar.builder(this.tabManager, this.width);
		this.tabs = new ArrayList<>();
		for (var category : this.root.getCategories()) {
			var tab = new ConfigCategoryTab<>(this, category, this.config);
			tabNavigationBarBuilder.addTabs(tab);
			this.tabs.add(tab);
		}
		this.tabNavigationBar = tabNavigationBarBuilder.build();
		this.initTabs(this.tabNavigationBar);
		this.addRenderableWidget(this.tabNavigationBar);
		// Create buttons directly instead of using LinearLayout.horizontal()
		var buttonWidth = 200;
		var buttonSpacing = 8;
		var totalWidth = buttonWidth * 2 + buttonSpacing;
		var startX = (this.width - totalWidth) / 2;
		var buttonY = this.height - 26;
		this.quitButton = Button.builder(this.getQuitLabel(), b -> this.onClose()).bounds(startX, buttonY, buttonWidth, 20).build();
		this.saveAndQuitButton = Button.builder(this.getSaveLabel(), b -> this.saveAndQuit())
			.bounds(startX + buttonWidth + buttonSpacing, buttonY, buttonWidth, 20)
			.build();
		this.addRenderableWidget(this.quitButton);
		this.addRenderableWidget(this.saveAndQuitButton);
		this.saveAndQuitButton.active = !this.root.isActiveValue(this.config) && this.root.validate(this.config) == null;
		this.layout.visitWidgets(abstractWidget -> {
			abstractWidget.setTabOrderGroup(1);
			this.addRenderableWidget(abstractWidget);
		});
		// Restore last selected tab or default to first tab
		int cachedTabIndex = lastSelectedTabCache.getOrDefault(this.cacheKey, 0);
		if (cachedTabIndex >= this.tabs.size()) cachedTabIndex = 0;
		this.tabNavigationBar.selectTab(cachedTabIndex, false);
		this.repositionElements();
	}
	private void initTabs(TabNavigationBar bar) {
		var i = 0;
		for (var child : bar.children())
			if (child instanceof TabButton tabButton) {
				this.tabs.get(i).setTabButton(tabButton);
				++i;
			}
	}
	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (Minecraft.getInstance().level == null) this.panoramaRenderer.render(partialTick, 1.0F);
		// Render the current tab's list
		var currentTab = this.tabManager.getCurrentTab();
		if (currentTab instanceof ConfigCategoryTab<?> configTab) configTab.getList().render(guiGraphics, mouseX, mouseY, partialTick);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
	}
	@Override
	protected void repositionElements() {
		this.refresh();
		if (this.tabNavigationBar != null) {
			this.tabNavigationBar.setWidth(this.width);
			this.tabNavigationBar.arrangeElements();
			var i = this.tabNavigationBar.getRectangle().bottom();
			var screenRectangle = new ScreenRectangle(0, i, this.width, this.height - this.layout.getFooterHeight() - i);
			this.tabManager.setTabArea(screenRectangle);
			this.layout.setHeaderHeight(i);
			this.layout.arrangeElements();
			// Manually call doLayout on all tabs to ensure list bounds are set
			for (var tab : this.tabs) tab.doLayout(screenRectangle);
		}
		// 动态调整底部按钮位置
		if (this.quitButton != null && this.saveAndQuitButton != null) {
			var buttonWidth = 200;
			var buttonSpacing = 8;
			var totalWidth = buttonWidth * 2 + buttonSpacing;
			var startX = (this.width - totalWidth) / 2;
			var buttonY = this.height - 26;
			this.quitButton.setX(startX);
			this.quitButton.setY(buttonY);
			this.quitButton.setWidth(buttonWidth);
			this.saveAndQuitButton.setX(startX + buttonWidth + buttonSpacing);
			this.saveAndQuitButton.setY(buttonY);
			this.saveAndQuitButton.setWidth(buttonWidth);
		}
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.tabNavigationBar.keyPressed(keyCode)) {
			this.cacheCurrentTabIndex();
			return true;
		}
		// Forward to current tab's list
		var currentTab = this.tabManager.getCurrentTab();
		if (currentTab instanceof ConfigCategoryTab<?> configTab)
			if (configTab.getList().keyPressed(keyCode, scanCode, modifiers)) return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// Forward to current tab's list first
		var currentTab = this.tabManager.getCurrentTab();
		if (currentTab instanceof ConfigCategoryTab<?> configTab) if (configTab.getList().mouseClicked(mouseX, mouseY, button)) return true;
		var result = super.mouseClicked(mouseX, mouseY, button);
		// Cache tab index after mouse click (may have clicked a tab)
		this.cacheCurrentTabIndex();
		return result;
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		var currentTab = this.tabManager.getCurrentTab();
		if (currentTab instanceof ConfigCategoryTab<?> configTab)
			if (configTab.getList().mouseReleased(mouseX, mouseY, button)) return true;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		var currentTab = this.tabManager.getCurrentTab();
		if (currentTab instanceof ConfigCategoryTab<?> configTab)
			if (configTab.getList().mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		var currentTab = this.tabManager.getCurrentTab();
		if (currentTab instanceof ConfigCategoryTab<?> configTab) if (configTab.getList().mouseScrolled(mouseX, mouseY, delta)) return true;
		return super.mouseScrolled(mouseX, mouseY, delta);
	}
	@Override
	public boolean charTyped(char chr, int modifiers) {
		var currentTab = this.tabManager.getCurrentTab();
		if (currentTab instanceof ConfigCategoryTab<?> configTab) if (configTab.getList().charTyped(chr, modifiers)) return true;
		return super.charTyped(chr, modifiers);
	}
	private void cacheCurrentTabIndex() {
		for (var i = 0; i < this.tabs.size(); i++)
			if (this.tabManager.getCurrentTab() == this.tabs.get(i)) {
				lastSelectedTabCache.put(this.cacheKey, i);
				break;
			}
	}
	@Override
	public void onClose() {
		if (this.root.isActiveValue(this.config)) {
			// no changes, no need to confirm
			this.getMinecraft().setScreen(this.previous);
			return;
		}
		this.getMinecraft().setScreen(new ConfirmScreen(
			confirmed -> this.getMinecraft().setScreen(confirmed ? this.previous : this),
			Translation.QUIT_CONFIRM_TITLE,
			Translation.QUIT_CONFIRM_WARNING,
			Translation.QUIT_CONFIRM_LABEL,
			Translation.CANCEL_LABEL
		));
	}
	public void saveAndQuit() {
		var restartRequired = this.root.restartRequired(this.config);
		this.root.writeEditingToConfig(this.config);
		this.onSave.accept(this.config);
		if (restartRequired) this.getMinecraft().setScreen(new ConfirmScreen(
			confirmed -> {
				if (confirmed) this.getMinecraft().stop();
				else this.getMinecraft().setScreen(this.previous);
			},
			Translation.RESTART_REQUIRED_TITLE,
			Translation.RESTART_REQUIRED_LABEL,
			Translation.QUIT_GAME,
			Translation.IGNORE_RESTART_LABEL
		));
		else this.getMinecraft().setScreen(this.previous);
	}
	public @NotNull Minecraft getMinecraft() {
		return Objects.requireNonNull(this.minecraft);
	}
	public int getHeaderHeight() {
		return this.layout.getHeaderHeight();
	}
	public int getFooterHeight() {
		return this.layout.getFooterHeight();
	}
	public void refresh() {
		this.tabs.forEach(ConfigCategoryTab::refresh);
		var hasEntryError = this.tabs.stream().anyMatch(ConfigCategoryTab::hasEntryError);
		this.saveAndQuitButton.active = !this.root.isActiveValue(this.config) && this.root.validate(this.config) == null && !hasEntryError;
		this.quitButton.setMessage(this.getQuitLabel());
		this.saveAndQuitButton.setMessage(this.getSaveLabel(hasEntryError));
	}
	private Component getQuitLabel() {
		return this.root.isActiveValue(this.config) ? Translation.CANCEL_LABEL : Translation.QUIT_UNSAVED_LABEL;
	}
	private Component getSaveLabel(boolean hasEntryError) {
		return this.root.validate(this.config) == null && !hasEntryError ? Translation.SAVE_LABEL : Translation.CANNOT_SAVE_LABEL;
	}
	private Component getSaveLabel() {
		return getSaveLabel(false);
	}
}
