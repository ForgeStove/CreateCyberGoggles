package io.github.forgestove.create_cyber_goggles.config.gui;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.forgestove.create_cyber_goggles.config.tree.RootConfigNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.*;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
public final class ConfigScreen<C> extends Screen {
	private static final Component CANCEL_LABEL = CommonComponents.GUI_CANCEL;
	private static final Component QUIT_UNSAVED_LABEL = Component.translatable("create_cyber_goggles.config.quit.unsaved");
	private static final Component SAVE_LABEL = Component.translatable("create_cyber_goggles.config.save");
	private static final Component CANNOT_SAVE_LABEL = Component.translatable("create_cyber_goggles.config.cannot_save");
	private static final Component QUIT_CONFIRM_LABEL = Component.translatable("create_cyber_goggles.config.quit.confirm");
	private static final Component QUIT_CONFIRM_TITLE = Component.translatable("create_cyber_goggles.config.quit.confirm.title");
	private static final Component QUIT_CONFIRM_WARNING = Component.translatable("create_cyber_goggles.config.quit.confirm.warning");
	private static final Component RESTART_REQUIRED_LABEL = Component.translatable("create_cyber_goggles.config.restart_required");
	private static final Component RESTART_REQUIRED_TITLE = Component.translatable("create_cyber_goggles.config.restart_required.title");
	private static final Component EXIT_MINECRAFT_LABEL = Component.translatable("create_cyber_goggles.config.exit_minecraft");
	private static final Component IGNORE_RESTART_LABEL = Component.translatable("create_cyber_goggles.config.ignore_restart");
	/** Cache for last selected tab index per config screen type */
	private static final Map<String, Integer> lastSelectedTabCache = new HashMap<>();
	private final RootConfigNode<C> root;
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
		var footerLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
		this.quitButton = footerLayout.addChild(Button.builder(this.getQuitLabel(), b -> this.onClose()).width(200).build());
		this.saveAndQuitButton = footerLayout.addChild(Button.builder(this.getSaveLabel(), b -> this.saveAndQuit()).width(200).build());
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
	public void render(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		RenderSystem.enableBlend();
		guiGraphics.blit(FOOTER_SEPARATOR, 0, this.height - this.getFooterHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
		RenderSystem.disableBlend();
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
		}
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.tabNavigationBar.keyPressed(keyCode)) {
			this.cacheCurrentTabIndex();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		var result = super.mouseClicked(mouseX, mouseY, button);
		// Cache tab index after mouse click (may have clicked a tab)
		this.cacheCurrentTabIndex();
		return result;
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
			QUIT_CONFIRM_TITLE,
			QUIT_CONFIRM_WARNING,
			QUIT_CONFIRM_LABEL,
			CANCEL_LABEL
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
			}, RESTART_REQUIRED_TITLE, RESTART_REQUIRED_LABEL, EXIT_MINECRAFT_LABEL, IGNORE_RESTART_LABEL
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
		return this.root.isActiveValue(this.config) ? CANCEL_LABEL : QUIT_UNSAVED_LABEL;
	}
	private Component getSaveLabel(boolean hasEntryError) {
		return this.root.validate(this.config) == null && !hasEntryError ? SAVE_LABEL : CANNOT_SAVE_LABEL;
	}
	private Component getSaveLabel() {
		return getSaveLabel(false);
	}
}

