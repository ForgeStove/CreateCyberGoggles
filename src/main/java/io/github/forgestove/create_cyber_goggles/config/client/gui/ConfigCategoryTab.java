package io.github.forgestove.create_cyber_goggles.config.client.gui;
import io.github.forgestove.create_cyber_goggles.config.client.gui.entry.*;
import io.github.forgestove.create_cyber_goggles.config.client.gui.util.GuiUtil;
import io.github.forgestove.create_cyber_goggles.config.tree.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
public final class ConfigCategoryTab<C> implements Tab {
	private final ConfigScreen<C> screen;
	private final CategoryConfigNode<C> category;
	private final C config;
	private final Component title;
	private final ConfigEntryList list;
	private final EntryTypeRegistry<C> entryTypeRegistry;
	private final Set<CategoryConfigNode<C>> expandedSubCategories = new HashSet<>();
	private TabButton tabButton;
	public ConfigCategoryTab(ConfigScreen<C> screen, CategoryConfigNode<C> category, C config, EntryTypeRegistry<C> entryTypeRegistry) {
		this.screen = screen;
		this.category = category;
		this.config = config;
		this.entryTypeRegistry = entryTypeRegistry;
		title = category.getTitle();
		collectDefaultExpanded(category);
		list = new ConfigEntryList(
			this,
			getMinecraft(),
			screen.width,
			screen.height - screen.getHeaderHeight() - screen.getFooterHeight(),
			screen.getHeaderHeight(),
			ConfigEntry.HEIGHT + ConfigEntry.GAP,
			buildEntries(category)
		);
	}
	@NotNull
	@Override
	public Component getTabTitle() {
		return title;
	}
	@Override
	public void visitChildren(Consumer<AbstractWidget> consumer) {
		consumer.accept(list);
	}
	@Override
	public void doLayout(ScreenRectangle screenRectangle) {
		list.setRectangle(screenRectangle.width(), screenRectangle.height(), screenRectangle.left(), screenRectangle.top());
	}
	public List<ConfigEntry> buildEntries(CategoryConfigNode<C> node) {
		return buildEntries(node, 0);
	}
	private @NotNull List<ConfigEntry> buildEntries(@NotNull CategoryConfigNode<C> node, int depth) {
		var entries = new ArrayList<ConfigEntry>();
		for (var child : node.getChildren())
			if (child instanceof ValueConfigNode<C, ?> valueNode) {
				var entry = entryTypeRegistry.createValueEntry(this, valueNode);
				if (entry instanceof TabLifecycle l) l.onAttachedToTab(this);
				entry.setIndent(depth * ConfigEntry.INDENT_PX);
				entries.add(entry);
			} else if (child instanceof CategoryConfigNode<C> subNode) {
				var expanded = expandedSubCategories.contains(subNode);
				entries.add(entryTypeRegistry.createCategoryEntry(
					this, subNode, expanded, depth, () -> {
						if (expandedSubCategories.contains(subNode)) expandedSubCategories.remove(subNode);
						else expandedSubCategories.add(subNode);
						list.replaceAllEntries(buildEntries(category));
					}
				));
				if (expanded) entries.addAll(buildEntries(subNode, depth + 1));
			}
		return entries;
	}
	private void collectDefaultExpanded(CategoryConfigNode<C> node) {
		for (var child : node.getChildren()) {
			if (!(child instanceof CategoryConfigNode<C> subNode)) continue;
			if (subNode.isDefaultExpanded()) expandedSubCategories.add(subNode);
			collectDefaultExpanded(subNode);
		}
	}
	@NotNull
	public Minecraft getMinecraft() {
		return Objects.requireNonNull(screen.getMinecraft());
	}
	public ConfigScreen<C> getScreen() {
		return screen;
	}
	public void refresh() {
		var hasChanged = !category.isActiveValue(config);
		var hasError = category.validate(config) != null || hasEntryError();
		tabButton.setMessage(GuiUtil.styleAsState(title, hasError, hasChanged));
		list.refresh();
	}
	public boolean hasEntryError() {
		return list.hasEntryError();
	}
	public C getConfig() {
		return config;
	}
	public void setTabButton(TabButton tabButton) {
		this.tabButton = tabButton;
	}
}
