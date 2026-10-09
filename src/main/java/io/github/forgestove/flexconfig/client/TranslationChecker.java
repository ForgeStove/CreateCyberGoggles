package io.github.forgestove.flexconfig.client;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.platform.Platform;
import io.github.forgestove.flexconfig.tree.RootConfigNode;
import net.minecraft.client.resources.language.I18n;
import org.slf4j.Logger;
/** 开发期检查配置用到的翻译键是否齐全；检查时机见 {@link ClientLifecycleHandler#tick()}。 */
public final class TranslationChecker {
	private static final Logger LOGGER = LogUtils.getLogger();
	/** 检查一棵配置树的翻译键；缺失的按行打印，便于直接照抄进语言文件。 */
	public static void check(RootConfigNode<?, ?> root) {
		if (!Platform.get().isDevelopmentEnvironment()) return;
		var language = ClientUtil.mc.getLanguageManager().getSelected();
		var missing = root.translationKeys().stream().filter(key -> !I18n.exists(key)).toList();
		if (missing.isEmpty()) return;
		LOGGER.warn(
			"Config '{}' is missing {} translation key(s) for language '{}'; the UI will show the raw keys:",
			root.fileName(),
			missing.size(),
			language
		);
		missing.forEach(key -> LOGGER.warn("  {}", key));
	}
}
