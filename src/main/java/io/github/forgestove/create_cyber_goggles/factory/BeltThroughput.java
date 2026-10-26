package io.github.forgestove.create_cyber_goggles.factory;
import java.util.*;
public class BeltThroughput {
	private final Deque<Integer> history = new ArrayDeque<>();
	private double rate;
	private int lastTotalItems;
	public void tick(int totalItems) {
		int passed = Math.max(0, lastTotalItems - totalItems);
		lastTotalItems = totalItems;
		history.addLast(passed);
		if (history.size() > 60) history.pollFirst();
		rate = (rate + history.stream().mapToInt(Integer::intValue).average().orElse(0) * 20) / 2;
	}
	public double rate() {
		return rate;
	}
}
