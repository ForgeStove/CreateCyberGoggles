package io.github.forgestove.create_cyber_goggles.config.gui.util;
import net.minecraft.util.Mth;

import java.util.function.*;
public class SmoothScrool {
	private final DoubleConsumer scrollSetter;
	private final Supplier<Double> scrollGetter;
	private final Supplier<Integer> maxScrollGetter;
	private double targetScroll;
	private double currentScroll;
	private boolean initialized;
	public SmoothScrool(DoubleConsumer scrollSetter, Supplier<Double> scrollGetter, Supplier<Integer> maxScrollGetter) {
		this.scrollSetter = scrollSetter;
		this.scrollGetter = scrollGetter;
		this.maxScrollGetter = maxScrollGetter;
	}
	public void tick(float delta) {
		if (!initialized) {
			currentScroll = scrollGetter.get();
			targetScroll = currentScroll;
			initialized = true;
		}
		currentScroll = Mth.lerp(delta, currentScroll, targetScroll);
		scrollSetter.accept(currentScroll);
	}
	public void onMouseScroll(double vertical, double itemHeight) {
		if (!initialized) {
			currentScroll = scrollGetter.get();
			targetScroll = currentScroll;
			initialized = true;
		}
		targetScroll = Mth.clamp(targetScroll - vertical * itemHeight, 0, maxScrollGetter.get());
	}
	public void sync() {
		currentScroll = scrollGetter.get();
		targetScroll = currentScroll;
	}
}
