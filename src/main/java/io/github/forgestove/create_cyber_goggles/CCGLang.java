package io.github.forgestove.create_cyber_goggles;
import net.createmod.catnip.lang.*;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Contract;
public class CCGLang extends Lang {
	@Contract(value = " -> new", pure = true)
	public static LangBuilder builder() {
		return new LangBuilder(CCG.ID);
	}
	public static LangBuilder translate(String langKey, Object... args) {
		return builder().translate(langKey, args);
	}
	public static LangBuilder text(String text) {
		return builder().text(text);
	}
	public static LangBuilder text(ChatFormatting format, String literalText) {
		return builder().text(format, literalText);
	}
	public static LangBuilder text(int color, String literalText) {
		return builder().text(color, literalText);
	}
	public static LangBuilder number(double n) {
		return builder().text(LangNumberFormat.format(n));
	}
}
