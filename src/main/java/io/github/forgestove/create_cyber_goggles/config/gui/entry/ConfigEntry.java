package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;
public abstract class ConfigEntry extends Entry<ConfigEntry> {
	@Nullable
	public List<FormattedCharSequence> getTooltip() {
		return null;
	}
	public void refresh() {}
	/**
	 * Returns true if this entry has an error that should prevent saving.
	 */
	public boolean hasError() {
		return false;
	}
}
