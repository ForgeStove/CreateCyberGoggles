package io.github.forgestove.create_cyber_goggles.mixin.misc.config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import io.github.forgestove.create_cyber_goggles.foundation.util.CatnipConfigLocalization;
import io.github.forgestove.flexconfig.api.ConfigTranslationKeys;
import net.createmod.catnip.config.ui.*;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SubMenuConfigScreen.class)
public abstract class SubMenuConfigScreenMixin {
	@Shadow protected ModConfigSpec spec;
	@Shadow protected UnmodifiableConfig configGroup;
	@Shadow protected String title;
	@Inject(method = "init", at = @At("HEAD"))
	private void localizeTitle(CallbackInfo ci) {
		var modId = ConfigScreen.modID;
		if (modId == null) return;
		var path = CatnipConfigLocalization.findGroupPath(spec, configGroup);
		if (path == null || path.isEmpty()) return;
		var key = ConfigTranslationKeys.category(modId, path);
		if (I18n.exists(key)) title = I18n.get(key);
	}
}
