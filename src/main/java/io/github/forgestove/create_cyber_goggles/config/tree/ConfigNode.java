package io.github.forgestove.create_cyber_goggles.config.tree;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
public interface ConfigNode<C> {
	@Nullable Component getTooltip();
	void resetToDefault();
	void resetToActive(C config);
	boolean restartRequired(C config);
	boolean isDefaultValue(C config);
	boolean isActiveValue(C config);
	@Nullable Component validate(C config);
	void copy(C from, C to);
	void writeEditingToConfig(C config);
	default String getPath() {
		return "";
	}
}
