package io.github.forgestove.create_cyber_goggles.core.util;
import com.demonwav.mcdev.annotations.Translatable;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
/**
 * Create 命名空间的翻译包装，供 IDE 解析短键。
 */
public final class CCGCreateLang {
	public static @NotNull LangBuilder translate(
		@Translatable(prefix = Create.ID + '.', foldMethod = true) String langKey,
		Object... args
	) {
		return CreateLang.translate(langKey, args);
	}
	public static @NotNull MutableComponent translateDirect(
		@Translatable(prefix = Create.ID + '.', foldMethod = true) String langKey,
		Object... args
	) {
		return CreateLang.translateDirect(langKey, args);
	}
	public static @NotNull LangBuilder builder() {
		return CreateLang.builder();
	}
	public static @NotNull LangBuilder number(double number) {
		return CreateLang.number(number);
	}
	public static @NotNull LangBuilder text(String text) {
		return CreateLang.text(text);
	}
}
