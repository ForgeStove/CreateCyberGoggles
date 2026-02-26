package io.github.forgestove.create_cyber_goggles.config.tree;
import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.util.Objects;
import java.util.function.UnaryOperator;
public final class CategoryConfigNode<C> implements ConfigNode<C> {
	public static final Component MULTIPLE_ERRORS = Component.translatable("config.ui.validator.multiple_errors");
	private String name;
	private Component title;
	private Component tooltip;
	private ImmutableList<ConfigNode<C>> children;
	private CategoryConfigNode() {
	}
	public static <C> Builder<C> builder() {
		return new Builder<>();
	}
	@NotNull
	@Override
	public String getName() {
		return this.name;
	}
	@NotNull
	@Override
	public Component getTitle() {
		return this.title;
	}
	@Nullable
	@Override
	public Component getTooltip() {
		return this.tooltip;
	}
	@Nullable
	@Override
	public Component getPrefix() {
		return null;
	}
	@Override
	public void resetToDefault() {
		this.children.forEach(ConfigNode::resetToDefault);
	}
	@Override
	public void resetToActive(C config) {
		this.children.forEach(child -> child.resetToActive(config));
	}
	@Override
	public boolean restartRequired(C config) {
		return this.children.stream().anyMatch(configNode -> configNode.restartRequired(config));
	}
	@Override
	public boolean isDefaultValue(C config) {
		return this.children.stream().allMatch(node -> node.isDefaultValue(config));
	}
	@Override
	public boolean isActiveValue(C config) {
		return this.children.stream().allMatch(node -> node.isActiveValue(config));
	}
	@Nullable
	@Override
	public Component validate(C config) {
		Component error = null;
		for (var node : this.children) {
			var result = node.validate(config);
			if (result != null) {
				if (error != null) return MULTIPLE_ERRORS;
				error = result;
			}
		}
		return error;
	}
	@NotNull
	public ImmutableList<ConfigNode<C>> getChildren() {
		return this.children;
	}
	@Override
	public void copy(C from, C to) {
		this.children.forEach(node -> node.copy(from, to));
	}
	@Override
	public void writeEditingToConfig(C config) {
		this.children.forEach(node -> node.writeEditingToConfig(config));
	}
	@SuppressWarnings("unused")
	public static final class Builder<C> {
		private CategoryConfigNode<C> node;
		private ImmutableList.Builder<ConfigNode<C>> childrenBuilder;
		private Builder() {
			this.node = new CategoryConfigNode<>();
			this.childrenBuilder = ImmutableList.builder();
		}
		public Builder<C> name(String name) {
			this.node.name = name;
			return this;
		}
		public Builder<C> title(Component title) {
			this.node.title = title;
			return this;
		}
		public Builder<C> tooltip(Component tooltip) {
			this.node.tooltip = tooltip;
			return this;
		}
		public <T, V> Builder<C> value(UnaryOperator<ValueConfigNode.Builder<C, T, V>> valueBuilder) {
			this.childrenBuilder.add(valueBuilder.apply(ValueConfigNode.builder()).category(this.node).build());
			return this;
		}
		public Builder<C> category(UnaryOperator<Builder<C>> categoryBuilder) {
			this.childrenBuilder.add(categoryBuilder.apply(new Builder<>()).build());
			return this;
		}
		public CategoryConfigNode<C> build() {
			var n = this.node;
			Objects.requireNonNull(n.name);
			Objects.requireNonNull(n.title);
			n.children = this.childrenBuilder.build();
			this.node = null;
			this.childrenBuilder = null;
			return n;
		}
	}
}
