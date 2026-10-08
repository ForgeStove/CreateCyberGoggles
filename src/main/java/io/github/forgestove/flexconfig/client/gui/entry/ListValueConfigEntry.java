package io.github.forgestove.flexconfig.client.gui.entry;
import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import org.jetbrains.annotations.Nullable;

import java.util.*;
/**
 * 逗号分隔的列表编辑器；空串表示空列表，也接受 {@code [a, b]} 形式的输入。
 * 元素类型取自字段声明的 {@code List<T>}，支持数值、布尔、字符串与枚举。
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class ListValueConfigEntry<C> extends GenericValueConfigEntry<C, List> {
	public ListValueConfigEntry(ConfigCategoryTab<C, List> tab, ValueConfigNode<C, List> node) {
		super(
			tab,
			node,
			text -> parse(node.getElementType(), text),
			text -> isValid(node.getElementType(), text),
			ListValueConfigEntry::format
		);
	}
	private static List<Object> parse(@Nullable Class<?> elementType, String input) {
		var body = input.trim();
		if (body.startsWith("[") && body.endsWith("]")) body = body.substring(1, body.length() - 1).trim();
		var result = new ArrayList<>();
		if (body.isEmpty()) return result;
		for (var part : body.split(",")) {
			var token = part.trim();
			if (!token.isEmpty()) result.add(parseElement(elementType, token));
		}
		return result;
	}
	private static boolean isValid(@Nullable Class<?> elementType, String input) {
		try {
			parse(elementType, input);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	private static Object parseElement(@Nullable Class<?> elementType, String token) {
		if (elementType == null || elementType == String.class) return token;
		if (elementType == Integer.class) return Integer.parseInt(token);
		if (elementType == Long.class) return Long.parseLong(token);
		if (elementType == Double.class) return Double.parseDouble(token);
		if (elementType == Float.class) return Float.parseFloat(token);
		if (elementType == Short.class) return Short.parseShort(token);
		if (elementType == Byte.class) return Byte.parseByte(token);
		if (elementType == Boolean.class) return Boolean.parseBoolean(token);
		if (elementType.isEnum()) return Enum.valueOf((Class<? extends Enum>) elementType, token);
		throw new IllegalArgumentException("Unsupported list element type: " + elementType.getName());
	}
	private static String format(@Nullable List list) {
		if (list == null) return "";
		var parts = new ArrayList<String>(list.size());
		for (var element : list) if (element != null) parts.add(String.valueOf(element));
		return String.join(", ", parts);
	}
}
