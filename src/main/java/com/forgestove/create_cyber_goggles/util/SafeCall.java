package com.forgestove.create_cyber_goggles.util;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;
/**
 * 一个类似于{@link Callable}的函数式接口。
 * <p>它返回一个结果，或者在无法计算结果时返回null。
 * <p>它的主要目的是在发生异常时返回null，而不是抛出{@link Throwable}。
 *
 * @param <V> 返回结果的类型
 */
@SuppressWarnings("unused")
public interface SafeCall<V> {
	/**
	 * 执行一个{@link SafeCall}对象，并在发生异常时返回null。
	 *
	 * @param safeCall 需要执行的{@link SafeCall}对象
	 * @param <T>      返回值类型
	 * @return {@link SafeCall}对象的返回值，或null（如果发生异常）
	 */
	static <T> @Nullable T call(SafeCall<T> safeCall) {
		try {
			return safeCall.call();
		} catch (Throwable ignored) {
			return null;
		}
	}
	/**
	 * 计算结果，或者在无法计算结果时抛出异常。
	 *
	 * @return 计算结果
	 * @throws Throwable 如果无法计算结果
	 */
	V call() throws Throwable;
}
