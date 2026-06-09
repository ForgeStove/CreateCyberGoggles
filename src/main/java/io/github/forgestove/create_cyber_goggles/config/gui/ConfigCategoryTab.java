package io.github.forgestove.create_cyber_goggles.config.gui;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.create_cyber_goggles.config.Translation;
import io.github.forgestove.create_cyber_goggles.config.gui.entry.*;
import io.github.forgestove.create_cyber_goggles.config.gui.screen.ConfigScreen;
import io.github.forgestove.create_cyber_goggles.config.tree.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.awt.Color;
import java.util.*;
import java.util.function.Consumer;
public final class ConfigCategoryTab<C> implements Tab {
	private final ConfigScreen<C> screen;
	private final CategoryConfigNode<C> category;
	private final C config;
	private final Component title;
	private final ConfigEntryList list;
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
		(tab, node) -> new KeybindValueConfigEntry<>(tab, cast(node))
	);
	@Nullable private TabButton tabButton;
	public ConfigCategoryTab(ConfigScreen<C> screen, CategoryConfigNode<C> category, C config) {
		this.screen = screen;
		this.category = category;
		this.config = config;
		title = category.getTitle();
		List<ConfigEntry> entries = new ArrayList<>();
		category.getChildren().forEach(node -> {
			if (node instanceof ValueConfigNode<C, ?> valueNode) entries.add(createValueEntry(valueNode));
			else if (node instanceof CategoryConfigNode<C> categoryNode) entries.addAll(createSubCategoryEntries(categoryNode));
		});
		list = new ConfigEntryList(
			this,
			getMinecraft(),
			screen.width,
			screen.height - screen.getHeaderHeight() - screen.getFooterHeight(),
			screen.getHeaderHeight(),
			22,
			entries
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
			if (entry.getKey().isAssignableFrom(type)) return entry.getValue().create(this, valueNode);
		return new TextConfigEntry(this, Translation.UNSUPPORTED_TYPE.copy().append(type.getSimpleName()).withStyle(ChatFormatting.RED));
	}
	private List<ConfigEntry> createSubCategoryEntries(CategoryConfigNode<C> categoryNode) {
		var entries = new ArrayList<ConfigEntry>(categoryNode.getChildren().size() + 1);
		entries.add(new CategoryTitleConfigEntry(this, categoryNode.getTitle()));
		for (var node : categoryNode.getChildren())
			if (node instanceof ValueConfigNode<C, ?> valueNode) entries.add(createValueEntry(valueNode));
		return entries;
	}
	@NotNull
	public Minecraft getMinecraft() {
		return Objects.requireNonNull(screen.getMinecraft());
	}
	public ConfigScreen<C> getScreen() {
		return screen;
	}
	public void refresh() {
		if (tabButton == null) return;
		var hasChanged = !category.isActiveValue(config);
		var hasError = category.validate(config) != null || hasEntryError();
		tabButton.setMessage(GuiUtil.styleAsState(title, hasError, hasChanged));
		list.refreshEntries();
	}
	public C getConfig() {
		return config;
	}
	public void setTabButton(@Nullable TabButton tabButton) {
		this.tabButton = tabButton;
	}
	public boolean hasEntryError() {
		return list.hasEntryError();
	}
	public boolean handleKeyCapture(int keyCode) {
		return list.handleKeyCapture(keyCode);
	}
	public boolean handleMouseCapture(int button) {
		return list.handleMouseCapture(button);
	}
	public boolean isCapturingKeybind() {
		return list.isCapturingKeybind();
	}
	@FunctionalInterface
	private interface EntryFactory<C> {
		ConfigEntry create(ConfigCategoryTab<C> tab, ValueConfigNode<C, ?> node);
	}
}
