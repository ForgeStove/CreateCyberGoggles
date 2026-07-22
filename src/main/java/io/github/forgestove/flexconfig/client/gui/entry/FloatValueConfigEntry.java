package io.github.forgestove.flexconfig.client.gui.entry;
import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;

import java.util.regex.Pattern;
public final class FloatValueConfigEntry<C> extends GenericValueConfigEntry<C, Float> {
	public static final Pattern FLOAT_PATTERN = Pattern.compile("-?\\d*(\\.\\d*)?");
	public FloatValueConfigEntry(ConfigCategoryTab<C, Float> tab, ValueConfigNode<C, Float> node) {
		super(tab, node, s -> isZero(s) ? 0f : Float.parseFloat(s), s -> isZero(s) || FLOAT_PATTERN.matcher(s).matches());
	}
}
