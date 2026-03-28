package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;

import java.util.regex.Pattern;
public final class LongValueConfigEntry<C> extends GenericValueConfigEntry<C, Long> {
	private static final Pattern LONG_PATTERN = Pattern.compile("-?\\d*");
	public LongValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, Long, Long> valueNode) {
		super(tab, valueNode, s -> isZero(s) ? 0L : Long.parseLong(s), s -> isZero(s) || LONG_PATTERN.matcher(s).matches());
	}
}
