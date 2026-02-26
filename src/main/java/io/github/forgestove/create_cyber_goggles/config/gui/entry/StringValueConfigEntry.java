package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
public final class StringValueConfigEntry<C> extends GenericValueConfigEntry<C, String> {
	public StringValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, String, String> valueNode) {
		super(tab, valueNode, s -> s, s -> true);
	}
}
