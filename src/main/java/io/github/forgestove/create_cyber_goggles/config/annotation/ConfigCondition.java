package io.github.forgestove.create_cyber_goggles.config.annotation;
import net.neoforged.fml.loading.LoadingModList;

import java.lang.reflect.Field;
import java.util.Arrays;
/**
 * A condition that must be satisfied for a config category or field to be loaded
 * into the config system.
 * <p>
 * Implementations must provide a no-arg constructor.
 */
@FunctionalInterface
public interface ConfigCondition {
	/**
	 * Evaluate a {@link Condition} annotation. Returns {@code true} if all
	 * conditions (both {@code value} and {@code condition}) are satisfied.
	 */
	static boolean evaluate(Condition condition) {
		var mod = condition.value();
		if (!mod.isEmpty() && LoadingModList.get().getModFileById(mod) == null) return false;
		return Arrays.stream(condition.condition()).allMatch(ConfigCondition::evaluate);
	}
	/**
	 * Check whether a field carries a {@link Condition} annotation and, if so,
	 * evaluate it. Fields without {@link Condition} always pass.
	 */
	static boolean evaluate(Field field) {
		var condition = field.getAnnotation(Condition.class);
		return condition == null || evaluate(condition);
	}
	/**
	 * Instantiate and evaluate a single condition class.
	 */
	private static boolean evaluate(Class<? extends ConfigCondition> conditionClass) {
		try {
			return conditionClass.getDeclaredConstructor().newInstance().test();
		} catch (Exception e) {
			throw new RuntimeException("Failed to evaluate config condition '%s'".formatted(conditionClass.getSimpleName()), e);
		}
	}
	/**
	 * @return {@code true} if the condition is met and the associated config
	 * 	element should be loaded, {@code false} otherwise
	 */
	boolean test();
}
