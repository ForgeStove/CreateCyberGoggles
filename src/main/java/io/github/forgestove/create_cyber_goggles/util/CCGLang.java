package io.github.forgestove.create_cyber_goggles.util;
import com.demonwav.mcdev.annotations.Translatable;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.*;
public class CCGLang {
	@Contract(value = " -> new", pure = true)
	public static @NotNull CCGLangBuilder of() {
		return new CCGLangBuilder(CCG.ID);
	}
	public static @NotNull MutableComponent translateDirect(
		@Translatable(prefix = CCG.ID + '.', foldMethod = true) String path,
		ChatFormatting format,
		Object... args
	) {
		return Component.translatable(CCG.ID + "." + path, args).withStyle(format);
	}
	public static @NotNull MutableComponent translateDirect(
		@Translatable(prefix = CCG.ID + '.', foldMethod = true) String path,
		Object... args
	) {
		return Component.translatable(CCG.ID + "." + path, args);
	}
}
