package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public final class CategoryTitleConfigEntry extends ConfigEntry {
	private final Minecraft minecraft;
	private final Component label;
	public CategoryTitleConfigEntry(ConfigCategoryTab<?> tab, Component label) {
		super();
		minecraft = tab.getMinecraft();
		this.label = label;
	}
	@NotNull
	@Override
	public List<? extends NarratableEntry> narratables() {
		return List.of(new NarratableEntry() {
			@NotNull
			@Override
			public NarrationPriority narrationPriority() {
				return NarrationPriority.HOVERED;
			}
			@Override
			public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
				narrationElementOutput.add(NarratedElementType.TITLE, label);
			}
		});
	}
	@Override
	public void render(
		GuiGraphics guiGraphics,
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
		guiGraphics.drawCenteredString(minecraft.font, label, x + entryWidth / 2, y + 5, -1);
	}
	@NotNull
	@Override
	public List<? extends GuiEventListener> children() {
		return List.of();
	}
}
