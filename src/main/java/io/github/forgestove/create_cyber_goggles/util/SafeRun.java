package io.github.forgestove.create_cyber_goggles.util;
/**
 * 一个类似于{@link Runnable}的函数式接口。
 * <p>它返回一个结果，或者在无法计算结果时返回null。
 * <p>它的主要目的是在发生异常时忽略它，而不是抛出{@link Throwable}。
 */
@SuppressWarnings("unused")
public interface SafeRun {
	/**
	 * 执行一个{@link SafeRun}对象，并在发生异常时忽略它。
	 *
	 * @param safeRun 需要执行的{@link SafeRun}对象
	 */
	static void run(SafeRun safeRun) {
		try {
			safeRun.run();
		} catch (Throwable ignored) {}
	}
	/**
	 * 定义一个需要实现的抽象方法，用于执行操作。
	 * <p>实现此方法时，可以抛出任何类型的异常。</p>
	 *
	 * @throws Throwable 如果在执行过程中发生错误
	 */
	void run() throws Throwable;
}
