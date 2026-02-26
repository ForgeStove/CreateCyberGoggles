package io.github.forgestove.create_cyber_goggles.config.tree;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;
public interface ConfigNode<C> {
	@NotNull String getName();
	@NotNull Component getTitle();
	@Nullable Component getTooltip();
	@Nullable Component getPrefix();
	void resetToDefault();
	void resetToActive(C config);
	boolean restartRequired(C config);
	boolean isDefaultValue(C config);
	boolean isActiveValue(C config);
	@Nullable Component validate(C config);
	void copy(C from, C to);
	void writeEditingToConfig(C config);
}
