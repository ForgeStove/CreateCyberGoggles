package io.github.forgestove.create_cyber_goggles.mixin.misc.config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import io.github.forgestove.create_cyber_goggles.foundation.util.CatnipConfigLocalization;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.LabeledEntryAccessor;
import io.github.forgestove.flexconfig.api.ConfigTranslationKeys;
import net.createmod.catnip.config.ui.*;
import net.createmod.catnip.config.ui.entries.SubMenuEntry;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SubMenuEntry.class)
public abstract class SubMenuEntryMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void localize(SubMenuConfigScreen parent, String label, ModConfigSpec spec, UnmodifiableConfig group, CallbackInfo ci) {
		var modId = ConfigScreen.modID;
		if (modId == null) return;
		var path = CatnipConfigLocalization.findGroupPath(spec, group);
		if (path == null || path.isEmpty()) return;
		var key = ConfigTranslationKeys.category(modId, path);
		if (!I18n.exists(key)) return;
		((LabeledEntryAccessor) this).getLabel().withText(I18n.get(key));
	}
}
