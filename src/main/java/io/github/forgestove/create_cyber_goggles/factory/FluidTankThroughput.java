package io.github.forgestove.create_cyber_goggles.factory;
import java.util.*;
public class FluidTankThroughput {
	private final Deque<Integer> history = new ArrayDeque<>();
	private boolean initialized;
	private int lastAmount;
	private double rate;
	public void tick(int amount, int window) {
		if (!initialized) {
			initialized = true;
			lastAmount = amount;
			return;
		}
		history.addLast(Math.abs(amount - lastAmount));
		lastAmount = amount;
		while (history.size() > window) history.pollFirst();
		rate = history.stream().mapToInt(Integer::intValue).average().orElse(0);
	}
	public double rate() {
		return rate;
	}
}
