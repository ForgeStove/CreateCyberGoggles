package io.github.forgestove.flexconfig.api;
import net.neoforged.fml.loading.LoadingModList;

import java.lang.reflect.Field;
import java.util.Arrays;
/**
 * 配置分类或字段加载到配置系统时必须满足的条件。
 * <p>
 * 实现类必须提供无参构造函数。
 */
@FunctionalInterface
public interface ConfigCondition {
	/**
	 * 检查字段是否带有 {@link Condition} 注解，如果有则评估它。
	 * 没有 {@link Condition} 注解的字段始终通过。
	 */
	static boolean evaluate(Field field) {
		var condition = field.getAnnotation(Condition.class);
		return condition == null || evaluate(condition);
	}
	/**
	 * 评估 {@link Condition} 注解。如果所有条件（包括 {@code value} 和 {@code condition}）都满足，则返回 {@code true}。
	 */
	static boolean evaluate(Condition condition) {
		var mod = condition.value();
		if (!mod.isEmpty() && LoadingModList.get().getModFileById(mod) == null) return false;
		return Arrays.stream(condition.condition()).allMatch(ConfigCondition::evaluate);
	}
	/**
	 * 实例化并评估单个条件类。
	 */
	private static boolean evaluate(Class<? extends ConfigCondition> conditionClass) {
		try {
			return conditionClass.getDeclaredConstructor().newInstance().test();
		} catch (Exception e) {
			throw new RuntimeException("Failed to evaluate config condition '%s'".formatted(conditionClass.getSimpleName()), e);
		}
	}
	/**
	 * @return 如果条件满足且相关的配置元素应被加载，则返回 {@code true}，否则返回 {@code false}
	 */
	boolean test();
}
