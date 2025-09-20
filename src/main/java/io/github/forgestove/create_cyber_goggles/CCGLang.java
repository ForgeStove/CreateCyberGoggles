package io.github.forgestove.create_cyber_goggles;
import com.zurrtum.create.client.catnip.lang.*;
import org.jetbrains.annotations.*;
public class CCGLang extends Lang {
	@Contract(value = " -> new", pure = true)
	public static @NotNull LangBuilder builder() {
		return new LangBuilder(CCG.ID);
	}
	public static @NotNull LangBuilder translate(String langKey, Object... args) {
		return builder().translate(langKey, args);
	}
	public static @NotNull LangBuilder number(double number) {
		return builder().text(LangNumberFormat.format(number));
	}
	public static @NotNull LangBuilder text(String text) {
		return builder().text(text);
	}
}
