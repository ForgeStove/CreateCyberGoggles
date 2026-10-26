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
		Double value = evaluateDouble(text);
		return value == null ? null : Math.round(value);
	}
	/** 保留小数，供倍数这类不要求整数的场景使用 */
	public static Double evaluateDouble(String text) {
		if (text == null || text.isBlank()) return null;
		double value = new Parser(text).parse();
		if (Double.isNaN(value) || Double.isInfinite(value)) return null;
		return value;
	}
	private static final class Parser {
		private final String text;
		private int index;
		private Parser(String text) {
			this.text = text;
		}
		private static boolean isDigit(char c) {
			return c >= '0' && c <= '9';
		}
		private double parse() {
			double value = expression();
			skipSpaces();
			return index == text.length() ? value : Double.NaN;
		}
		private double expression() {
			double value = term();
			while (true) if (consume('+')) value += term();
			else if (consume('-')) value -= term();
			else return value;
		}
		private double term() {
			double value = unary();
			while (true) if (consume('*') || consume('x') || consume('X') || consume('×')) value *= unary();
			else if (consume('/') || consume('÷')) {
				double divisor = unary();
				if (divisor == 0) return Double.NaN;
				value /= divisor;
			} else return value;
		}
		private double unary() {
			if (consume('+')) return unary();
			if (consume('-')) return -unary();
			return primary();
		}
		private double primary() {
			if (consume('(')) {
				double value = expression();
				return consume(')') ? value : Double.NaN;
			}
			int start = index;
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
	}
}
