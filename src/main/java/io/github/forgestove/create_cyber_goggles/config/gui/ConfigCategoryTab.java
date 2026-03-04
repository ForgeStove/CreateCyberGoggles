package io.github.forgestove.create_cyber_goggles.config.gui;
import io.github.forgestove.create_cyber_goggles.config.Translation;
import io.github.forgestove.create_cyber_goggles.config.gui.entry.*;
import io.github.forgestove.create_cyber_goggles.config.tree.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;
public final class ConfigCategoryTab<C> implements Tab {
	private final ConfigScreen<C> screen;
	private final CategoryConfigNode<C> category;
	private final C config;
	private final Component title;
	private final Component titleChanged;
	private final Component titleError;
	private final Component titleErrorChanged;
	private final ConfigEntryList list;
	@Nullable private TabButton tabButton;
	public ConfigCategoryTab(ConfigScreen<C> screen, CategoryConfigNode<C> category, C config) {
		this.screen = screen;
		this.category = category;
		this.config = config;
		this.title = category.getTitle();
		this.titleChanged = title.copy().withStyle(ChatFormatting.ITALIC, ChatFormatting.YELLOW);
		this.titleError = title.copy().withStyle(ChatFormatting.RED);
		this.titleErrorChanged = title.copy().withStyle(ChatFormatting.ITALIC, ChatFormatting.RED);
		List<ConfigEntry> entries = new ArrayList<>();
		category.getChildren().forEach(node -> {
			if (node instanceof ValueConfigNode<C, ?, ?> valueNode) entries.add(this.createValueEntry(valueNode));
			else if (node instanceof CategoryConfigNode<C> categoryNode) entries.addAll(this.createSubCategoryEntries(categoryNode));
		});
		this.list = new ConfigEntryList(
			this,
			this.getMinecraft(),
			this.screen.width,
			this.screen.height - this.screen.getHeaderHeight() - this.screen.getFooterHeight(),
			this.screen.getHeaderHeight(),
			22,
			entries
		);
	}
	@NotNull
	@Override
	public Component getTabTitle() {
		return this.title;
	}
	@Override
	public void visitChildren(Consumer<AbstractWidget> consumer) {
		consumer.accept(this.list);
	}
	@Override
	public void doLayout(ScreenRectangle screenRectangle) {
		this.list.setRectangle(screenRectangle.width(), screenRectangle.height(), screenRectangle.left(), screenRectangle.top());
	}
	@FunctionalInterface
	private interface ConfigEntryFactory<C> {
		@Nullable ConfigEntry create(ConfigCategoryTab<C> tab, ValueConfigNode<C, ?, ?> node);
	}
	private static final Map<Class<?>, ConfigEntryFactory<?>> ENTRY_FACTORIES = Map.of(
		Boolean.class, (tab, node) -> new BooleanValueConfigEntry<>(tab, cast(node)),
		Integer.class, (tab, node) -> node.isColorValue()
			? new ColorValueConfigEntry<>(tab, cast(node), node.colorHasAlpha())
			: new IntegerValueConfigEntry<>(tab, cast(node)),
		Long.class, (tab, node) -> new LongValueConfigEntry<>(tab, cast(node)),
		Float.class, (tab, node) -> new FloatValueConfigEntry<>(tab, cast(node)),
		Double.class, (tab, node) -> new DoubleValueConfigEntry<>(tab, cast(node)),
		String.class, (tab, node) -> new StringValueConfigEntry<>(tab, cast(node))
	);
	@SuppressWarnings("unchecked")
	private static <C, T, V> ValueConfigNode<C, T, V> cast(ValueConfigNode<C, ?, ?> node) {
		return (ValueConfigNode<C, T, V>) node;
	}
	@SuppressWarnings("unchecked")
	private <T, V> ConfigEntry createValueEntry(ValueConfigNode<C, T, V> valueNode) {
		var type = valueNode.getType();
		// Handle Enum types specially since we need isAssignableFrom check
		if (Enum.class.isAssignableFrom(type))
			return new EnumValueConfigEntry<>(this, cast(valueNode));
		// Look up factory from the table
		var factory = (ConfigEntryFactory<C>) ENTRY_FACTORIES.get(type);
		if (factory != null) return factory.create(this, valueNode);
		// Unsupported type fallback
		return new TextConfigEntry(
			this,
			Translation.UNSUPPORTED_TYPE.copy().append(type.getSimpleName()).withStyle(ChatFormatting.RED)
		);
	}
	private List<ConfigEntry> createSubCategoryEntries(CategoryConfigNode<C> categoryNode) {
		var entries = new ArrayList<ConfigEntry>(categoryNode.getChildren().size() + 1);
		entries.add(new CategoryTitleConfigEntry(this, categoryNode.getTitle()));
		for (var node : categoryNode.getChildren())
			if (node instanceof ValueConfigNode<C, ?, ?> valueNode) entries.add(this.createValueEntry(valueNode));
		return entries;
	}
	@NotNull
	public Minecraft getMinecraft() {
		return Objects.requireNonNull(this.screen.getMinecraft());
	}
	public ConfigScreen<C> getScreen() {
		return this.screen;
	}
	public void refresh() {
		if (this.tabButton == null) return;
		Component newTitle;
		var hasChanged = !this.category.isActiveValue(this.config);
		var hasError = this.category.validate(this.config) != null || this.list.hasEntryError();
		if (hasError) newTitle = hasChanged ? this.titleErrorChanged : this.titleError;
		else newTitle = hasChanged ? this.titleChanged : this.title;
		tabButton.setMessage(newTitle);
		this.list.refreshEntries();
	}
	public boolean hasEntryError() {
		return this.list.hasEntryError();
	}
	public C getConfig() {
		return this.config;
	}
	public void setTabButton(@Nullable TabButton tabButton) {
		this.tabButton = tabButton;
	}
}

