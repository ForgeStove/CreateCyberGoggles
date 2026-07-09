package io.github.forgestove.config.client.gui.entry;
import io.github.forgestove.config.client.ClientUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.*;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public final class CategoryCollapsibleConfigEntry extends ConfigEntry {
	private static final String EXPANDED_PREFIX = "- ";
	private static final String COLLAPSED_PREFIX = "+ ";
	private final Component label;
	private final Runnable onToggle;
	private final boolean expanded;
	private final ClickWidget clickWidget;
	public CategoryCollapsibleConfigEntry(Component label, boolean expanded, int depth, Runnable onToggle) {
		this.label = label;
		this.expanded = expanded;
		this.onToggle = onToggle;
		setIndent(depth * INDENT_PX);
		clickWidget = new ClickWidget();
	}
	@NotNull
	@Override
	public List<? extends NarratableEntry> narratables() {
		return List.of(clickWidget);
	}
	@Override
	public void render(
		GuiGraphics gui,
		int index,
		int y,
		int x,
		int entryWidth,
		int entryHeight,
		int mouseX,
		int mouseY,
		boolean hovered,
		float delta
	) {
		var indent = getIndent();
		clickWidget.setRectangle(entryWidth, entryHeight, x, y);
		var prefix = expanded ? EXPANDED_PREFIX : COLLAPSED_PREFIX;
		var font = ClientUtil.mc.font;
		gui.drawString(font, prefix, x + indent, y + 5, 0xAAAAAA, false);
		gui.drawString(font, label.getVisualOrderText(), x + indent + font.width(prefix), y + 5, -1, false);
	}
	@NotNull
	@Override
	public List<? extends GuiEventListener> children() {
		return List.of(clickWidget);
	}
	public class ClickWidget extends AbstractWidget {
		private ClickWidget() {
			super(0, 0, 0, 0, Component.empty());
		}
		@Override
		protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {}
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button != 0) return false;
			ClientUtil.mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			onToggle.run();
			return true;
		}
		@NotNull
		@Override
		public NarrationPriority narrationPriority() {
			return NarrationPriority.HOVERED;
		}
		@Override
		public void updateWidgetNarration(@NotNull NarrationElementOutput output) {
			output.add(NarratedElementType.TITLE, label);
		}
	}
}
