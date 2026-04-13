package io.github.forgestove.create_cyber_goggles.config.gui;
import com.mojang.blaze3d.platform.InputConstants.Key;
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
import java.util.function.Consumer;
public final class ConfigCategoryTab<C> implements Tab {
	private static final Map<Class<?>, ConfigEntryFactory<?>> ENTRY_FACTORIES = Map.of(
		Boolean.class,
		(tab, node) -> new BooleanValueConfigEntry<>(tab, cast(node)),
		Integer.class,
		(tab, node) -> node.isColorValue()
			? new ColorValueConfigEntry<>(tab, cast(node), node.colorHasAlpha())
			: new IntegerValueConfigEntry<>(tab, cast(node)),
		Long.class,
		(tab, node) -> new LongValueConfigEntry<>(tab, cast(node)),
		Float.class,
		(tab, node) -> new FloatValueConfigEntry<>(tab, cast(node)),
		Double.class,
		(tab, node) -> new DoubleValueConfigEntry<>(tab, cast(node)),
		String.class,
		(tab, node) -> new StringValueConfigEntry<>(tab, cast(node)),
		Key.class,
		(tab, node) -> new KeybindValueConfigEntry<>(tab, cast(node))
	);
	private final ConfigScreen<C> screen;
	private final String modId;
	private final CategoryConfigNode<C> category;
	private final C config;
	private final Component title;
	private final Component titleChanged;
	private final Component titleError;
	private final Component titleErrorChanged;
	private final ConfigEntryList list;
	@Nullable private TabButton tabButton;
	public ConfigCategoryTab(ConfigScreen<C> screen, CategoryConfigNode<C> category, C config, String modId) {
		this.screen = screen;
		this.modId = modId;
		this.category = category;
		this.config = config;
		title = category.getTitle();
		titleChanged = title.copy().withStyle(ChatFormatting.ITALIC, ChatFormatting.YELLOW);
		titleError = title.copy().withStyle(ChatFormatting.RED);
		titleErrorChanged = title.copy().withStyle(ChatFormatting.ITALIC, ChatFormatting.RED);
		List<ConfigEntry> entries = new ArrayList<>();
		category.getChildren().forEach(node -> {
			if (node instanceof ValueConfigNode<C, ?, ?> valueNode) entries.add(createValueEntry(valueNode));
			else if (node instanceof CategoryConfigNode<C> categoryNode) entries.addAll(createSubCategoryEntries(categoryNode));
		});
		list = new ConfigEntryList(
			this,
			getMinecraft(),
			this.screen.width,
			this.screen.height - this.screen.getHeaderHeight() - this.screen.getFooterHeight(),
			this.screen.getHeaderHeight(),
			22,
			entries
		);
	}
	@SuppressWarnings("unchecked")
	private static <C, T, V> ValueConfigNode<C, T, V> cast(ValueConfigNode<C, ?, ?> node) {
		return (ValueConfigNode<C, T, V>) node;
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
	@SuppressWarnings("unchecked")
	private <T, V> ConfigEntry createValueEntry(ValueConfigNode<C, T, V> valueNode) {
		var type = valueNode.getType();
		if (Enum.class.isAssignableFrom(type))
			return new EnumValueConfigEntry<>(this, (ValueConfigNode<C, Enum<?>, Enum<?>>) valueNode, modId);
		var factory = (ConfigEntryFactory<C>) ENTRY_FACTORIES.get(type);
		if (factory != null) return factory.create(this, valueNode);
		return new TextConfigEntry(this, Translation.UNSUPPORTED_TYPE.copy().append(type.getSimpleName()).withStyle(ChatFormatting.RED));
	}
	private List<ConfigEntry> createSubCategoryEntries(CategoryConfigNode<C> categoryNode) {
		var entries = new ArrayList<ConfigEntry>(categoryNode.getChildren().size() + 1);
		entries.add(new CategoryTitleConfigEntry(this, categoryNode.getTitle()));
		for (var node : categoryNode.getChildren())
			if (node instanceof ValueConfigNode<C, ?, ?> valueNode) entries.add(createValueEntry(valueNode));
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
		Component newTitle;
		var hasChanged = !category.isActiveValue(config);
		var hasError = category.validate(config) != null || list.hasEntryError();
		if (hasError) newTitle = hasChanged ? titleErrorChanged : titleError;
		else newTitle = hasChanged ? titleChanged : title;
		tabButton.setMessage(newTitle);
		list.refreshEntries();
	}
	public boolean hasEntryError() {
		return list.hasEntryError();
	}
	public C getConfig() {
		return config;
	}
	public void setTabButton(@Nullable TabButton tabButton) {
		this.tabButton = tabButton;
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
	private interface ConfigEntryFactory<C> {
		@Nullable ConfigEntry create(ConfigCategoryTab<C> tab, ValueConfigNode<C, ?, ?> node);
	}
}

