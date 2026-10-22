package io.github.forgestove.create_cyber_goggles.core.util;
/** 四则算式求值：支持 + - * / x X × ÷ 与括号，非法输入返回 null。 */
public final class ExpressionParser {
	public static boolean isAllowed(String text) {
		return text.chars().allMatch(c -> isAllowedChar((char) c));
	}
	public static boolean isAllowedChar(char c) {
		return c >= '0' && c <= '9' || c == '.' || "+-*/xX×÷() ".indexOf(c) >= 0;
	}
	public static Long evaluate(String text) {
		if (text == null || text.isBlank()) return null;
		var value = new Parser(text).parse();
		if (Double.isNaN(value) || Double.isInfinite(value)) return null;
		return Math.round(value);
	}
	private static final class Parser {
		private final String text;
		private int index;
		private Parser(String text) {
			this.text = text;
		}
		private double parse() {
			var value = expression();
			skipSpaces();
			return index == text.length() ? value : Double.NaN;
		}
		private double expression() {
			var value = term();
			while (true) {
				if (consume('+')) value += term();
				else if (consume('-')) value -= term();
				else break;
			}
			return value;
		}
		private double term() {
			var value = unary();
			while (true) {
				if (consume('*') || consume('x') || consume('X') || consume('×')) value *= unary();
				else if (consume('/') || consume('÷')) {
					var divisor = unary();
					if (divisor == 0) return Double.NaN;
					value /= divisor;
				} else break;
			}
			return value;
		}
		private double unary() {
			if (consume('+')) return unary();
			if (consume('-')) return -unary();
			return primary();
		}
		private double primary() {
			if (consume('(')) {
				var value = expression();
				return consume(')') ? value : Double.NaN;
			}
			var start = index;
			while (index < text.length() && (isDigit(text.charAt(index)) || text.charAt(index) == '.')) index++;
			if (start == index) return Double.NaN;
			try {
				return Double.parseDouble(text.substring(start, index));
			} catch (NumberFormatException e) {
				return Double.NaN;
			}
		}
		private boolean consume(char c) {
			skipSpaces();
			if (index >= text.length() || text.charAt(index) != c) return false;
			index++;
			return true;
		}
		private void skipSpaces() {
			while (index < text.length() && text.charAt(index) == ' ') index++;
		}
		private static boolean isDigit(char c) {
			return c >= '0' && c <= '9';
		}
	}
}
