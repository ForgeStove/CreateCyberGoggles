package io.github.forgestove.create_cyber_goggles.config.gui;
import io.github.forgestove.create_cyber_goggles.config.gui.entry.ConfigEntry;
import io.github.forgestove.create_cyber_goggles.config.gui.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
public final class ConfigEntryList extends ContainerObjectSelectionList<ConfigEntry> {
	private final SmoothScrool smoothScrool = new SmoothScrool(this::setScrollAmount, this::scrollAmount, this::maxScrollAmount);
	private final Highlight highlight = new Highlight(() -> children().indexOf(getHovered()), this::getRowTop);
	public ConfigEntryList(
		Minecraft minecraft,
		int width,
		int height,
		int headerHeight,
		int itemHeight,
		@NotNull Iterable<ConfigEntry> entries
	) {
		super(minecraft, width, height, headerHeight, itemHeight);
		entries.forEach(this::addEntry);
	}
	@Override
	public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		smoothScrool.tick(delta);
		highlight.tick(gui, getX(), getY(), width, height, defaultEntryHeight, delta);
		super.renderWidget(gui, mouseX, mouseY, delta);
		var entry = getHovered();
		if (entry == null) return;
		var widgetTooltip = entry.getHoveredWidgetTooltip(mouseX, mouseY);
		if (widgetTooltip != null) {
			gui.setTooltipForNextFrame(minecraft.font, widgetTooltip.toCharSequence(minecraft), mouseX, mouseY);
			return;
		}
		if (entry.getTooltip() == null) return;
		var tooltip = entry.getTooltip();
		gui.setTooltipForNextFrame(minecraft.font, tooltip, mouseX, mouseY);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		smoothScrool.onMouseScroll(vertical, defaultEntryHeight);
		return true;
	}
	@Override
	public boolean mouseDragged(@NonNull MouseButtonEvent event, double dragX, double dragY) {
		var result = super.mouseDragged(event, dragX, dragY);
		// 同步拖拽后的滚动位置，防止 smoothScrool 在下帧覆盖掉
		smoothScrool.sync();
		return result;
	}
	@Override
	public int getRowWidth() {
		return width * 4 / 5;
	}
	public void refreshEntries() {
		children().forEach(ConfigEntry::refresh);
	}
	public boolean hasEntryError() {
		for (var configEntry : children()) if (configEntry.hasError()) return true;
		return false;
	}
}
