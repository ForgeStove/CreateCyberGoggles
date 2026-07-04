package io.github.forgestove.create_cyber_goggles.config.client.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.client.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;

import java.util.regex.Pattern;
public final class DoubleValueConfigEntry<C> extends GenericValueConfigEntry<C, Double> {
	public static final Pattern DOUBLE_PATTERN = Pattern.compile("-?\\d*(\\.\\d*)?");
	public DoubleValueConfigEntry(ConfigCategoryTab<C, Double> tab, ValueConfigNode<C, Double> node) {
		super(tab, node, s -> isZero(s) ? 0d : Double.parseDouble(s), s -> isZero(s) || DOUBLE_PATTERN.matcher(s).matches());
	}
}
