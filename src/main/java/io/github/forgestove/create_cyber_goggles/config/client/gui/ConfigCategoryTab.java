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
	public final C config;
	public final ConfigScreen<C, V> screen;
	public final CategoryConfigNode<C> node;
	public final ConfigEntryList list;
	private final Component title;
	private TabButton tabButton;
	public ConfigCategoryTab(ConfigScreen<C, V> screen, @NotNull CategoryConfigNode<C> node, C config) {
		this.screen = screen;
		this.node = node;
		this.config = config;
		title = node.getTitle();
		if (defaultsApplied.add(node)) collectDefaultExpanded(node);
		list = new ConfigEntryList(screen, buildEntries(node, 0));
	}
	@NotNull
	@Override
	public Component getTabTitle() {
		return title;
	}
	@Override
	public void visitChildren(@NotNull Consumer<AbstractWidget> consumer) {
		consumer.accept(list);
	}
	@Override
	public void doLayout(@NotNull ScreenRectangle screenRectangle) {
		list.setRectangle(screenRectangle.width(), screenRectangle.height(), screenRectangle.left(), screenRectangle.top());
	}
	private @NotNull List<ConfigEntry> buildEntries(@NotNull CategoryConfigNode<C> node, int depth) {
		var entries = new ArrayList<ConfigEntry>();
		for (var child : node.getChildren())
			if (child instanceof ValueConfigNode<C, ?> valueNode) {
				var entry = EntryTypeRegistry.createValueEntry(this, valueNode);
				if (entry instanceof TabLifecycle lifecycle) lifecycle.onAttachedToTab(this);
				entry.setIndent(depth * ConfigEntry.INDENT_PX);
				entries.add(entry);
			} else if (child instanceof CategoryConfigNode<C> subNode) {
				var expanded = expandedSubCategories.contains(subNode);
				Runnable onToggle = () -> {
					if (expandedSubCategories.contains(subNode)) expandedSubCategories.remove(subNode);
					else expandedSubCategories.add(subNode);
					list.replaceAllEntries(buildEntries(this.node, 0));
				};
				entries.add(EntryTypeRegistry.createCategoryEntry(subNode.getTitle(), expanded, depth, onToggle));
				if (expanded) entries.addAll(buildEntries(subNode, depth + 1));
			}
		return entries;
	}
	private void collectDefaultExpanded(@NotNull CategoryConfigNode<C> node) {
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
	public static @NotNull Component styleAsState(@NotNull Component component, boolean hasError, boolean hasChanged) {
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
