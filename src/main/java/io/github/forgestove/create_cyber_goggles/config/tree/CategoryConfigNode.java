package io.github.forgestove.create_cyber_goggles.config.tree;
import com.google.common.collect.ImmutableList;
import io.github.forgestove.create_cyber_goggles.config.Translation;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.util.function.UnaryOperator;
public final class CategoryConfigNode<C> implements ConfigNode<C> {
	private Component title;
	private Component tooltip;
	private ImmutableList<ConfigNode<C>> children;
	public static <C> Builder<C> builder() {
		return new Builder<>();
	}
	@NotNull
	public Component getTitle() {
		return title;
	}
	@Nullable
	@Override
	public Component getTooltip() {
		return tooltip;
	}
	@Override
	public void resetToDefault() {
		children.forEach(ConfigNode::resetToDefault);
	}
	@Override
	public void resetToActive(C config) {
		children.forEach(child -> child.resetToActive(config));
	}
	@Override
	public boolean restartRequired(C config) {
		return children.stream().anyMatch(configNode -> configNode.restartRequired(config));
	}
	@Override
	public boolean isDefaultValue(C config) {
		return children.stream().allMatch(node -> node.isDefaultValue(config));
	}
	@Override
	public boolean isActiveValue(C config) {
		return children.stream().allMatch(node -> node.isActiveValue(config));
	}
	@Nullable
	@Override
	public Component validate(C config) {
		Component error = null;
		for (var node : children) {
			var result = node.validate(config);
			if (result == null) continue;
			if (error == null) error = result;
			else return Translation.MULTIPLE_ERRORS;
		}
		return error;
	}
	@NotNull
	public ImmutableList<ConfigNode<C>> getChildren() {
		return children;
	}
	@Override
	public void copy(C from, C to) {
		children.forEach(node -> node.copy(from, to));
	}
	@Override
	public void writeEditingToConfig(C config) {
		children.forEach(node -> node.writeEditingToConfig(config));
	}
	public static final class Builder<C> {
		private CategoryConfigNode<C> node;
		private ImmutableList.Builder<ConfigNode<C>> childrenBuilder;
		private Builder() {
			node = new CategoryConfigNode<>();
			childrenBuilder = ImmutableList.builder();
		}
		public Builder<C> title(Component title) {
			node.title = title;
			return this;
		}
		public Builder<C> tooltip(Component tooltip) {
			node.tooltip = tooltip;
			return this;
		}
		public <V> Builder<C> value(UnaryOperator<ValueConfigNode.Builder<C, V>> valueBuilder) {
			childrenBuilder.add(valueBuilder.apply(ValueConfigNode.builder()).category(node).build());
			return this;
		}
		public Builder<C> category(UnaryOperator<Builder<C>> categoryBuilder) {
			childrenBuilder.add(categoryBuilder.apply(new Builder<>()).build());
			return this;
		}
		public CategoryConfigNode<C> build() {
			var n = node;
			n.children = childrenBuilder.build();
			node = null;
			childrenBuilder = null;
			return n;
		}
	}
}
