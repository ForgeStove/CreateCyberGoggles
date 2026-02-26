package io.github.forgestove.create_cyber_goggles.config.gui.entry;

import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import java.util.regex.Pattern;

public final class FloatValueConfigEntry<C> extends GenericValueConfigEntry<C, Float> {
    private static final Pattern FLOAT_PATTERN = Pattern.compile("-?\\d*(\\.\\d*)?");
    public FloatValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, Float, Float> valueNode) {
        super(
            tab,
            valueNode,
            s -> s.isEmpty() || s.equals("-") ? 0f : Float.parseFloat(s),
            s -> s.isEmpty() || s.equals("-") || FLOAT_PATTERN.matcher(s).matches()
        );
    }
}
