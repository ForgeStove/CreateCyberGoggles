package io.github.forgestove.config.client.gui.factory;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Contract;

import java.util.function.*;
public class SmoothScroll {
	private static final float SPEED = 0.75F;
	private final DoubleConsumer scrollSetter;
	private final Supplier<Double> scrollGetter;
	private final Supplier<Integer> maxScrollGetter;
	private double targetScroll;
	private double currentScroll;
	private double lastSetScroll;
	private boolean initialized;
	@Contract(pure = true)
	public SmoothScroll(DoubleConsumer scrollSetter, Supplier<Double> scrollGetter, Supplier<Integer> maxScrollGetter) {
		this.scrollSetter = scrollSetter;
		this.scrollGetter = scrollGetter;
		this.maxScrollGetter = maxScrollGetter;
	}
	public void tick(float delta) {
		var actualScroll = scrollGetter.get();
		if (!initialized) {
			currentScroll = actualScroll;
			targetScroll = actualScroll;
			lastSetScroll = actualScroll;
			initialized = true;
			return;
		}
		// 检测外部滚动变化（拖拽、clear→add、API 调用），自动同步并跳过本帧插值
		if (Math.abs(actualScroll - lastSetScroll) > 1.0E-4) {
			currentScroll = actualScroll;
			targetScroll = actualScroll;
			lastSetScroll = actualScroll;
			return;
		}
		var progress = 1.0 - Math.pow(1 - SPEED, delta);
		currentScroll = Mth.lerp(progress, currentScroll, targetScroll);
		scrollSetter.accept(currentScroll);
		lastSetScroll = currentScroll;
	}
	public boolean onMouseScroll(double vertical, double itemHeight) {
		if (!initialized) {
			currentScroll = scrollGetter.get();
			targetScroll = currentScroll;
			initialized = true;
		}
		targetScroll = Mth.clamp(targetScroll - vertical * itemHeight, 0, maxScrollGetter.get());
		return true;
	}
}
