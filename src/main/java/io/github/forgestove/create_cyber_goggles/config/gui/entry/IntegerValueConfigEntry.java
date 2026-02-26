package io.github.forgestove.create_cyber_goggles.config.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;

import java.util.regex.Pattern;
public final class IntegerValueConfigEntry<C> extends GenericValueConfigEntry<C, Integer> {
	private static final Pattern INTEGER_PATTERN = Pattern.compile("-?\\d*");
	public IntegerValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, Integer, Integer> valueNode) {
		super(
			tab,
			valueNode,
			s -> s.isEmpty() || s.equals("-") ? 0 : Integer.parseInt(s),
			s -> s.isEmpty() || s.equals("-") || INTEGER_PATTERN.matcher(s).matches()
		);
	}
}
