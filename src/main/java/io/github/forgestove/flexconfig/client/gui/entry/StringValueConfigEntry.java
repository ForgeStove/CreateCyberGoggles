package io.github.forgestove.flexconfig.client.gui.entry;
import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
public final class StringValueConfigEntry<C> extends GenericValueConfigEntry<C, String> {
	public StringValueConfigEntry(ConfigCategoryTab<C, String> tab, ValueConfigNode<C, String> node) {
		super(tab, node, s -> s, s -> true);
	}
}
