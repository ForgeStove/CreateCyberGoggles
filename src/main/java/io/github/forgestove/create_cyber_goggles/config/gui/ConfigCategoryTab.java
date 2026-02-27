package io.github.forgestove.create_cyber_goggles.config.gui;
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
		for (var node : category.getChildren()) {
			if (node.getPrefix() != null) entries.add(new PrefixTextConfigEntry(this, node.getPrefix()));
			if (node instanceof ValueConfigNode<C, ?, ?> valueNode) entries.add(this.createValueEntry(valueNode));
			else if (node instanceof CategoryConfigNode<C> categoryNode) entries.addAll(this.createSubCategoryEntries(categoryNode));
		}
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
	public void visitChildren(@NotNull Consumer<AbstractWidget> consumer) {
	}
	@Override
	public void doLayout(ScreenRectangle screenRectangle) {
		this.list.updateSize(
			screenRectangle.width(),
			screenRectangle.height(),
			screenRectangle.top(),
			screenRectangle.top() + screenRectangle.height()
		);
	}
	@SuppressWarnings(
		{
			"unchecked",
			"rawtypes"
		}
	)
	private <T, V> ConfigEntry createValueEntry(ValueConfigNode<C, T, V> valueNode) {
		var type = valueNode.getType();
		if (type.equals(Boolean.class)) return new BooleanValueConfigEntry<>(this, (ValueConfigNode<C, Boolean, Boolean>) valueNode);
		else if (Enum.class.isAssignableFrom(type)) return new EnumValueConfigEntry<>(this, (ValueConfigNode<C, Enum, Enum>) valueNode);
		else if (type.equals(Integer.class)) {
			// Check if it's a color value
			if (valueNode.isColorValue())
				return new ColorValueConfigEntry<>(this, (ValueConfigNode<C, Integer, Integer>) valueNode, valueNode.colorHasAlpha());
			return new IntegerValueConfigEntry<>(this, (ValueConfigNode<C, Integer, Integer>) valueNode);
		} else if (type.equals(Float.class)) return new FloatValueConfigEntry<>(this, (ValueConfigNode<C, Float, Float>) valueNode);
		else if (type.equals(Double.class)) return new DoubleValueConfigEntry<>(this, (ValueConfigNode<C, Double, Double>) valueNode);
		else if (type.equals(Long.class)) return new LongValueConfigEntry<>(this, (ValueConfigNode<C, Long, Long>) valueNode);
		else if (type.equals(String.class)) return new StringValueConfigEntry<>(this, (ValueConfigNode<C, String, String>) valueNode);
		else return new PrefixTextConfigEntry(
				this,
				Translation.UNSUPPORTED_TYPE.copy().append(valueNode.getType().getSimpleName()).withStyle(ChatFormatting.RED)
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
	public ConfigEntryList getList() {
		return this.list;
	}
	public void setTabButton(@Nullable TabButton tabButton) {
		this.tabButton = tabButton;
	}
}
