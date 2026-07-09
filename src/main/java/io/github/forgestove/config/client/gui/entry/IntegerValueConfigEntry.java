package io.github.forgestove.config.client.gui.entry;
import io.github.forgestove.config.client.gui.ConfigCategoryTab;
import io.github.forgestove.config.tree.ValueConfigNode;

import java.util.regex.Pattern;
public final class IntegerValueConfigEntry<C> extends GenericValueConfigEntry<C, Integer> {
	public static final Pattern INTEGER_PATTERN = Pattern.compile("-?\\d*");
	public IntegerValueConfigEntry(ConfigCategoryTab<C, Integer> tab, ValueConfigNode<C, Integer> node) {
		super(tab, node, s -> isZero(s) ? 0 : Integer.parseInt(s), s -> isZero(s) || INTEGER_PATTERN.matcher(s).matches());
	}
}
