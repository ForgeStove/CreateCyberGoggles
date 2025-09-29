package io.github.forgestove.create_cyber_goggles.core.util;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.lang.*;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.getGradientColor;
public class CCGLang extends Lang {
	@Contract(value = " -> new", pure = true)
	public static @NotNull LangBuilder builder() {
		return new LangBuilder(CCG.ID);
	}
	public static @NotNull LangBuilder translate(String langKey, Object... args) {
		return builder().translate(langKey, args);
	}
	public static @NotNull LangBuilder translate(ChatFormatting format, String langKey, Object... args) {
		return translate(langKey, args).style(format);
	}
	public static @NotNull LangBuilder text(String text) {
		return builder().text(text);
	}
	public static @NotNull LangBuilder text(ChatFormatting format, String literalText) {
		return builder().text(format, literalText);
	}
	public static @NotNull LangBuilder number(double number) {
		return text(LangNumberFormat.format(number));
	}
	public static @NotNull LangBuilder number(int number) {
		return text(String.valueOf(number));
	}
	public static @NotNull LangBuilder number(ChatFormatting format, int number) {
		return text(format, String.valueOf(number));
	}
	public static @NotNull LangBuilder progress(float progress, int totalBars) {
		var filledBars = (int) (progress * totalBars);
		return text(ChatFormatting.GREEN, "|".repeat(filledBars)).text(ChatFormatting.GRAY, "|".repeat(totalBars - filledBars));
	}
	public static @NotNull LangBuilder fraction(int current, int total) {
		return number(current).color(getGradientColor((float) current / total))
			.text(ChatFormatting.GRAY, " / ")
			.add(number(total).style(ChatFormatting.DARK_GRAY));
	}
}
