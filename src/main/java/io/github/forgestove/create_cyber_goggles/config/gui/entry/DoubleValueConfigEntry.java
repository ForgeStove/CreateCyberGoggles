package io.github.forgestove.create_cyber_goggles.config.gui.entry;

import io.github.forgestove.create_cyber_goggles.config.gui.ConfigCategoryTab;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import java.util.regex.Pattern;

public final class DoubleValueConfigEntry<C> extends GenericValueConfigEntry<C, Double> {
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("-?\\d*(\\.\\d*)?");
    public DoubleValueConfigEntry(ConfigCategoryTab<C> tab, ValueConfigNode<C, Double, Double> valueNode) {
        super(
            tab,
            valueNode,
            s -> s.isEmpty() || s.equals("-") ? 0d : Double.parseDouble(s),
            s -> s.isEmpty() || s.equals("-") || DOUBLE_PATTERN.matcher(s).matches()
        );
    }
}
