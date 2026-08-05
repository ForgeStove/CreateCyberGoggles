package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.content.logistics.BigItemStack;

import java.util.Locale;
public final class AmountUtil {
	private static final int MB_PER_BUCKET = 1000;
	private static final int MB_PER_KILOBUCKET = MB_PER_BUCKET * 1000;
	public static String formatItemCount(int count) {
		if (count >= 1_000_000) return count / 1_000_000 + "m";
		if (count >= 10_000) return count / 1_000 + "k";
		if (count >= 1_000) return (count * 10 / 1000) / 10f + "k";
		return String.valueOf(count);
	}
	/** 流体数量缩写：mB/B/KB 分档，规则与 FluidLogistics 一致 */
	public static String formatFluidAmount(int amountMb) {
		if (amountMb >= BigItemStack.INF) return "∞";
		if (amountMb >= MB_PER_KILOBUCKET) return formatCompact(amountMb, MB_PER_KILOBUCKET, "KB");
		if (amountMb >= 100) return formatCompact(amountMb, MB_PER_BUCKET, "B");
		return amountMb + "mB";
	}
	private static String formatCompact(int amount, int unitSize, String suffix) {
		if (amount >= unitSize && amount % unitSize == 0) return amount / unitSize + suffix;
		if (amount / unitSize <= 10) {
			var value = Math.floor(amount / (unitSize / 10.0)) / 10.0;
			return String.format(Locale.ROOT, "%.1f%s", value, suffix);
		}
		return String.format(Locale.ROOT, "%.0f%s", Math.floor(amount / (float) unitSize), suffix);
	}
}
