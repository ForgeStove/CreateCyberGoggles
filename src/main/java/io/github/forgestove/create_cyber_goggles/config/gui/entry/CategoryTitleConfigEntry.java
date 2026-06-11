package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
	public void extractContent(GuiGraphicsExtractor gui, int mouseX, int mouseY, boolean hovered, float delta) {
		gui.centeredText(minecraft.font, label, getX() + getWidth() / 2, getY() + 5, -1);
	}
	@NotNull
	@Override
	public List<? extends GuiEventListener> children() {
		return List.of();
	}
}
