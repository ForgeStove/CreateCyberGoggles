package io.github.forgestove.create_cyber_goggles.util.contract;
public interface Self<T> {
	@SuppressWarnings("unchecked")
	default T thiz() {
		return (T) this;
	}
}
