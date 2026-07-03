package io.github.forgestove.create_cyber_goggles.config.client.gui;
import io.github.forgestove.create_cyber_goggles.config.client.gui.entry.ConfigEntry;
import io.github.forgestove.create_cyber_goggles.config.client.gui.factory.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public final class ConfigEntryList extends ContainerObjectSelectionList<ConfigEntry> {
	private final ConfigCategoryTab<?> tab;
	private final SmoothScroll smoothScroll = new SmoothScroll(this::setScrollAmount, this::getScrollAmount, this::getMaxScroll);
	private final Highlight highlight = new Highlight(() -> children().indexOf(getHovered()), this::getRowTop);
	public ConfigEntryList(
		ConfigCategoryTab<?> tab,
		Minecraft minecraft,
		int width,
		int height,
		int headerHeight,
		int itemHeight,
		@NotNull Iterable<ConfigEntry> entries
	) {
		super(minecraft, width, height, headerHeight, itemHeight);
		this.tab = tab;
		this.headerHeight = -3;
		entries.forEach(this::addEntry);
	}
	public void refresh() {
		var entries = children();
		for (var entry : entries)
			if (entry instanceof CrossRefreshable cr && cr.beginCrossEntryRefresh(entries)) break;
		entries.forEach(ConfigEntry::refresh);
	}
	public boolean hasEntryError() {
		for (var configEntry : children()) if (configEntry.hasError()) return true;
		return false;
	}
	public void replaceAllEntries(List<ConfigEntry> newEntries) {
		children().clear();
		newEntries.forEach(this::addEntry);
	}
	@Override
	public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		smoothScroll.tick(delta);
		highlight.tick(gui, getX(), getY(), width, height, itemHeight, delta);
		super.renderWidget(gui, mouseX, mouseY, delta);
		var entry = getHovered();
		if (entry == null) return;
		var widgetTooltip = entry.getHoveredWidgetTooltip(mouseX, mouseY);
		if (widgetTooltip != null) {
			tab.getScreen().setTooltipForNextRenderPass(widgetTooltip.toCharSequence(minecraft));
			return;
		}
		if (entry.getTooltip() != null) tab.getScreen().setTooltipForNextRenderPass(entry.getTooltip());
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		return smoothScroll.onMouseScroll(vertical, itemHeight);
	}
	@Override
	public int getRowWidth() {
		return width * 4 / 5;
	}
	@Override
	protected void renderListItems(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		var left = getRowLeft();
		var width = getRowWidth();
		var count = getItemCount();
		for (var i = 0; i < count; i++) {
			var top = getRowTop(i);
			var bottom = getRowBottom(i);
			if (bottom >= getY() && top <= getBottom()) renderItem(gui, mouseX, mouseY, delta, i, left, top, width, itemHeight);
		}
	}
}
