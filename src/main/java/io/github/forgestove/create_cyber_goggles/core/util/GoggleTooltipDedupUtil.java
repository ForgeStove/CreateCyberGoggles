package io.github.forgestove.create_cyber_goggles.core.util;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
public final class GoggleTooltipDedupUtil {
	public static @NotNull List<Component> dedupAdjacentLines(@NotNull List<Component> tooltip) {
		if (tooltip.isEmpty()) return tooltip;
		var deduped = new ArrayList<Component>(tooltip.size());
		var replacedDuplicate = false;
		for (var current : tooltip) {
			var previousKept = deduped.isEmpty() ? null : deduped.getLast();
			if (isDedupTarget(previousKept) && isDedupTarget(current) && isSimilarLine(previousKept, current)) {
				// 相邻行相似时保留较新的信息
				deduped.set(deduped.size() - 1, current);
				replacedDuplicate = true;
				continue;
			}
			// 如果刚刚替换了重复对，也删除紧随其后的空白分隔行
			if (replacedDuplicate && isBlankLine(current)) {
				replacedDuplicate = false;
				continue;
			}
			deduped.add(current);
			replacedDuplicate = false;
		}
		return cleanupBlankLines(deduped);
	}
	private static boolean isDedupTarget(Component line) {
		return line != null && !isIconLine(line) && !isBlankLine(line);
	}
	private static boolean isIconLine(@NotNull Component line) {
		return TooltipComponentUtil.hasIcon(line);
	}
	private static boolean isBlankLine(@NotNull Component line) {
		if (isIconLine(line)) return false;
		return line.getString().trim().isEmpty();
	}
	private static boolean isSimilarLine(@NotNull Component left, @NotNull Component right) {
		var leftText = normalizeText(left.getString());
		var rightText = normalizeText(right.getString());
		if (leftText.isEmpty() || rightText.isEmpty()) return false;
		if (leftText.equals(rightText)) return true;
		var min = Math.min(leftText.length(), rightText.length());
		var max = Math.max(leftText.length(), rightText.length());
		if (min < 8) return false;
		if (max - min > 3) return false;
		return leftText.contains(rightText) || rightText.contains(leftText);
	}
	private static @NotNull String normalizeText(@NotNull String text) {
		return text.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
	}
	private static @NotNull List<Component> cleanupBlankLines(@NotNull List<Component> tooltip) {
		var cleaned = new ArrayList<Component>(tooltip.size());
		var previousBlank = true;
		for (var line : tooltip) {
			var blank = isBlankLine(line);
			if (blank && previousBlank) continue;
			cleaned.add(line);
			previousBlank = blank;
		}
		while (!cleaned.isEmpty() && isBlankLine(cleaned.getLast())) cleaned.removeLast();
		return cleaned;
	}
}
