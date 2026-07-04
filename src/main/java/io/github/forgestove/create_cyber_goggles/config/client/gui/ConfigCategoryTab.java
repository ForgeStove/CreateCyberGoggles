package io.github.forgestove.create_cyber_goggles.config.client.gui;
import io.github.forgestove.create_cyber_goggles.config.client.gui.api.TabLifecycle;
import io.github.forgestove.create_cyber_goggles.config.client.gui.entry.ConfigEntry;
import io.github.forgestove.create_cyber_goggles.config.tree.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
public final class ConfigCategoryTab<C, V> implements Tab {
	private static final Set<CategoryConfigNode<?>> expandedSubCategories = new HashSet<>();
	private static final Set<CategoryConfigNode<?>> defaultsApplied = new HashSet<>();
	public final ConfigScreen<C, V> screen;
	public final CategoryConfigNode<C> node;
	public final C config;
	public final ConfigEntryList<C, V> list;
	private final Component title;
	private final EntryTypeRegistry<C> entryTypeRegistry;
	private TabButton tabButton;
	public ConfigCategoryTab(ConfigScreen<C, V> screen, CategoryConfigNode<C> node, C config, EntryTypeRegistry<C> entryTypeRegistry) {
		this.screen = screen;
		this.node = node;
		this.config = config;
		this.entryTypeRegistry = entryTypeRegistry;
		title = node.getTitle();
		if (defaultsApplied.add(node)) collectDefaultExpanded(node);
		list = new ConfigEntryList<>(screen, buildEntries(node));
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
	public @NotNull List<ConfigEntry> buildEntries(CategoryConfigNode<C> node) {
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
					subNode, expanded, depth, () -> {
						if (expandedSubCategories.contains(subNode)) expandedSubCategories.remove(subNode);
						else expandedSubCategories.add(subNode);
						list.replaceAllEntries(buildEntries(this.node));
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
	public void refresh() {
		var hasChanged = !node.isActiveValue(config);
		var hasError = node.validate(config) != null || hasEntryError();
		if (tabButton != null) tabButton.setMessage(styleAsState(title, hasError, hasChanged));
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
	public void setTabButton(TabButton tabButton) {
		this.tabButton = tabButton;
	}
}
