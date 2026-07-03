package io.github.forgestove.create_cyber_goggles.config.client.gui;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.create_cyber_goggles.config.client.Translation;
import io.github.forgestove.create_cyber_goggles.config.client.gui.entry.*;
import io.github.forgestove.create_cyber_goggles.config.client.gui.util.GuiUtil;
import io.github.forgestove.create_cyber_goggles.config.tree.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
public final class ConfigCategoryTab<C> implements Tab {
	private final ConfigScreen<C> screen;
	private final CategoryConfigNode<C> category;
	private final C config;
	private final Component title;
	private final ConfigEntryList list;
	private final Set<CategoryConfigNode<C>> expandedSubCategories = new HashSet<>();
	private final Map<Class<?>, EntryFactory<C>> entryFactories = Map.of(
		Enum.class,
		(tab, node) -> new EnumValueConfigEntry<>(tab, cast(node)),
		Boolean.class,
		(tab, node) -> new BooleanValueConfigEntry<>(tab, cast(node)),
		Integer.class,
		(tab, node) -> node.isColorValue() ? new ColorValueConfigEntry<>(tab, cast(node)) : new IntegerValueConfigEntry<>(tab, cast(node)),
		Long.class,
		(tab, node) -> new LongValueConfigEntry<>(tab, cast(node)),
		Float.class,
		(tab, node) -> new FloatValueConfigEntry<>(tab, cast(node)),
		Double.class,
		(tab, node) -> new DoubleValueConfigEntry<>(tab, cast(node)),
		String.class,
		(tab, node) -> new StringValueConfigEntry<>(tab, cast(node)),
		Color.class,
		(tab, node) -> new ColorValueConfigEntry<>(tab, cast(node)),
		Key.class,
		(tab, node) -> new KeybindValueConfigEntry<>(tab, cast(node)),
		Point.class,
		(tab, node) -> new PointValueConfigEntry<>(tab, cast(node))
	);
	private TabButton tabButton;
	public ConfigCategoryTab(ConfigScreen<C> screen, CategoryConfigNode<C> category, C config) {
		this.screen = screen;
		this.category = category;
		this.config = config;
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
	@SuppressWarnings("unchecked")
	private static <C, V> ValueConfigNode<C, V> cast(ValueConfigNode<C, ?> node) {
		return (ValueConfigNode<C, V>) node;
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
	private ConfigEntry createValueEntry(ValueConfigNode<C, ?> valueNode) {
		var type = valueNode.getValueType();
		for (var entry : entryFactories.entrySet())
			if (entry.getKey().isAssignableFrom(type)) {
				var configEntry = entry.getValue().create(this, valueNode);
				if (configEntry instanceof KeybindValueConfigEntry<?> keyEntry)
					keyEntry.setCaptureCallback(screen::onEntryCaptureChanged);
				return configEntry;
			}
		return new TextConfigEntry(this, Translation.UNSUPPORTED_TYPE.copy().append(type.getSimpleName()).withStyle(ChatFormatting.RED));
	}
	public List<ConfigEntry> buildEntries(CategoryConfigNode<C> node) {
		return buildEntries(node, 0);
	}
	private @NotNull List<ConfigEntry> buildEntries(@NotNull CategoryConfigNode<C> node, int depth) {
		var entries = new ArrayList<ConfigEntry>();
		for (var child : node.getChildren())
			if (child instanceof ValueConfigNode<C, ?> valueNode) {
				var entry = createValueEntry(valueNode);
				entry.setIndent(depth * CategoryCollapsibleConfigEntry.INDENT_PX);
				entries.add(entry);
			} else if (child instanceof CategoryConfigNode<C> subNode) {
				var expanded = expandedSubCategories.contains(subNode);
				entries.add(new CategoryCollapsibleConfigEntry(
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
	@FunctionalInterface
	private interface EntryFactory<C> {
		ConfigEntry create(ConfigCategoryTab<C> tab, ValueConfigNode<C, ?> node);
	}
}
