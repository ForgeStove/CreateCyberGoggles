package io.github.forgestove.create_cyber_goggles.config.client.gui;
import io.github.forgestove.create_cyber_goggles.config.client.ClientUtil;
import io.github.forgestove.create_cyber_goggles.config.client.gui.api.CrossRefreshable;
import io.github.forgestove.create_cyber_goggles.config.client.gui.entry.ConfigEntry;
import io.github.forgestove.create_cyber_goggles.config.client.gui.factory.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public final class ConfigEntryList extends ContainerObjectSelectionList<ConfigEntry> {
	private final Screen screen;
	private final Highlight highlight = new Highlight(() -> children().indexOf(getHovered()), this::getRowTop);
	private final SmoothScroll smoothScroll = new SmoothScroll(this::setScrollAmount, this::getScrollAmount, this::getMaxScroll);
	public ConfigEntryList(@NotNull ConfigScreen<?, ?> screen, @NotNull Iterable<ConfigEntry> entries) {
		super(
			ClientUtil.mc,
			screen.width,
			screen.height - screen.getHeaderHeight() - screen.getFooterHeight(),
			screen.getHeaderHeight(),
			ConfigEntry.HEIGHT + ConfigEntry.GAP
		);
		this.screen = screen;
		headerHeight = -3;
		entries.forEach(this::addEntry);
	}
	public void refresh() {
		for (var entry : children())
			if (entry instanceof CrossRefreshable refreshable && refreshable.beginCrossEntryRefresh(children())) break;
		children().forEach(ConfigEntry::refresh);
	}
	public boolean hasEntryError() {
		for (var configEntry : children()) if (configEntry.hasError()) return true;
		return false;
	}
	public void replaceAllEntries(@NotNull List<ConfigEntry> newEntries) {
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
			screen.setTooltipForNextRenderPass(widgetTooltip.toCharSequence(minecraft));
			return;
		}
		var tooltip = entry.getTooltip();
		if (tooltip != null) screen.setTooltipForNextRenderPass(tooltip);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		return smoothScroll.onMouseScroll(vertical, itemHeight);
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
	@Override
	public int getRowWidth() {
		return width * 4 / 5;
	}
}
