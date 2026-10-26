package io.github.forgestove.create_cyber_goggles.event.schematic;
import com.demonwav.mcdev.annotations.Translatable;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.util.CCGLang;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.MutableComponent;
/**
 * 蓝图预览与渲染共用的语言与配色。配色取自 Create: Blueprinted（MIT）。
 */
public final class SchematicLang {
	public static final int DARK_BLUE = 0x528FDE;
	public static final int LIGHT_BLUE = 0x94B5DD;
	public static final int LIGHT_GREEN = 0x8DD58F;
	public static final int ERROR_DARKER = 0xDA4A54;
	public static final int ERROR_PRIMARY = 0xE4656E;
	public static MutableComponent error(@Translatable(prefix = CCG.ID + ".error.", foldMethod = true) String path, Object... args) {
		return CCGLang.translateDirect("error." + path, args).withColor(ERROR_DARKER);
	}
	/** 超出宽度时截断并补省略号 */
	public static String truncate(Font font, String text, int maxWidth) {
		if (font.width(text) <= maxWidth) return text;
		var ellipsis = "...";
		return font.plainSubstrByWidth(text, maxWidth - font.width(ellipsis)) + ellipsis;
	}
}
