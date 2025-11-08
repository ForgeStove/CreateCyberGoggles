package io.github.forgestove.create_cyber_goggles.core.util;
public interface Self<T> {
	@SuppressWarnings("unchecked")
	default T self() {
		return (T) this;
	}
}
