package io.github.forgestove.create_cyber_goggles.config.client.gui.entry;
import io.github.forgestove.create_cyber_goggles.config.client.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;

import java.util.regex.Pattern;
public final class FloatValueConfigEntry<C> extends GenericValueConfigEntry<C, Float> {
	public static final Pattern FLOAT_PATTERN = Pattern.compile("-?\\d*(\\.\\d*)?");
	public FloatValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, Float> node) {
		super(tab, node, s -> isZero(s) ? 0f : Float.parseFloat(s), s -> isZero(s) || FLOAT_PATTERN.matcher(s).matches());
	}
}
