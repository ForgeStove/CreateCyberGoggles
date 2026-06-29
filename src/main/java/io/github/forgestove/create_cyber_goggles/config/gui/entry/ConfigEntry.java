package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;
public abstract class ConfigEntry extends Entry<ConfigEntry> {
	public static final int SIZE = 20;
	public static final int WIDTH = 100;
	public static final int HEIGHT = 20;
	public static final int GAP = 2;
	private int indent;
	/** 跳过 active 检查，确保禁用按钮也能显示 tooltip */
	public int getIndent() {
		return indent;
	}
	public void setIndent(int indent) {
		this.indent = indent;
	}
	@Nullable
	public List<FormattedCharSequence> getTooltip() {
		return null;
	}
	@Nullable
	public Tooltip getHoveredWidgetTooltip(int mouseX, int mouseY) {
		for (var child : children())
			if (child instanceof AbstractWidget widget && widget.visible && isMouseOverWidget(widget, mouseX, mouseY)) {
				var tooltip = widget.getTooltip();
				if (tooltip == null) continue;
				return tooltip;
			}
		return null;
	}
	private static boolean isMouseOverWidget(AbstractWidget widget, int mouseX, int mouseY) {
		return mouseX >= widget.getX()
			&& mouseY >= widget.getY()
			&& mouseX < widget.getX() + widget.getWidth()
			&& mouseY < widget.getY() + widget.getHeight();
	}
	public void refresh() {}
	/**
	 * Returns true if this entry has an error that should prevent saving.
	 */
	public boolean hasError() {
		return false;
	}
}
