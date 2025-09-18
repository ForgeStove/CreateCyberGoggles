package io.github.forgestove.create_cyber_goggles;
import net.createmod.catnip.lang.*;
public class CCGLang extends Lang {
	public static LangBuilder builder() {
		return new LangBuilder(CCG.ID);
	}
	public static LangBuilder translate(String langKey, Object... args) {
		return builder().translate(langKey, args);
	}
	public static LangBuilder number(double number) {
		return builder().text(LangNumberFormat.format(number));
	}
	public static LangBuilder text(String text) {
		return builder().text(text);
	}
}
