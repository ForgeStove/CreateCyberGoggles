package io.github.forgestove.create_cyber_goggles.config.client.gui;
import io.github.forgestove.create_cyber_goggles.config.client.gui.api.TabLifecycle;
import io.github.forgestove.create_cyber_goggles.config.client.gui.entry.ConfigEntry;
import io.github.forgestove.create_cyber_goggles.config.tree.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.Consumer;
public final class ConfigCategoryTab<C> implements Tab {
	private static final Set<CategoryConfigNode<?>> expandedSubCategories = new HashSet<>();
	private static final Set<CategoryConfigNode<?>> defaultsApplied = new HashSet<>();
	private final ConfigScreen<C> screen;
	private final CategoryConfigNode<C> category;
	private final C config;
	private final Component title;
	private final ConfigEntryList list;
	private final EntryTypeRegistry<C> entryTypeRegistry;
	private TabButton tabButton;
	public ConfigCategoryTab(ConfigScreen<C> screen, CategoryConfigNode<C> category, C config, EntryTypeRegistry<C> entryTypeRegistry) {
		this.screen = screen;
		this.category = category;
		this.config = config;
		this.entryTypeRegistry = entryTypeRegistry;
		title = category.getTitle();
		if (defaultsApplied.add(category)) collectDefaultExpanded(category);
		list = new ConfigEntryList(screen, buildEntries(category));
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
				entries.add(entryTypeRegistry.createCategoryEntry(subNode, expanded, depth, () -> {
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
	public ConfigScreen<C> getScreen() {
		return screen;
	}
	public void refresh() {
		var hasChanged = !category.isActiveValue(config);
		var hasError = category.validate(config) != null || hasEntryError();
		tabButton.setMessage(styleAsState(title, hasError, hasChanged));
		list.refresh();
	}
	public boolean hasEntryError() {
		return list.hasEntryError();
	}
	public static Component styleAsState(Component component, boolean hasError, boolean hasChanged) {
		var result = component.copy();
		if (hasError) result.withStyle(ChatFormatting.RED);
		else if (hasChanged) result.withStyle(ChatFormatting.YELLOW);
		if (hasChanged) result.withStyle(ChatFormatting.ITALIC);
		return result;
	}
	public C getConfig() {
		return config;
	}
	public CategoryConfigNode<C> getCategoryNode() {
		return category;
	}
	public void setTabButton(TabButton tabButton) {
		this.tabButton = tabButton;
	}
}
