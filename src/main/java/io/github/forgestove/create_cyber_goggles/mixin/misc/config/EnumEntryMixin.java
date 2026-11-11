package io.github.forgestove.create_cyber_goggles.mixin.misc.config;
import io.github.forgestove.flexconfig.api.ConfigTranslationKeys;
import net.createmod.catnip.config.ui.ConfigScreen;
import net.createmod.catnip.config.ui.entries.EnumEntry;
import net.createmod.catnip.gui.element.TextStencilElement;
import net.minecraft.client.resources.language.I18n;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(EnumEntry.class)
public abstract class EnumEntryMixin {
	@Shadow protected TextStencilElement valueText;
	@Inject(method = "onValueChange(Ljava/lang/Enum;)V", at = @At("TAIL"))
	private void localize(Enum<?> newValue, CallbackInfo ci) {
		var modId = ConfigScreen.modID;
		if (modId == null) return;
		var key = ConfigTranslationKeys.enumValue(modId, newValue);
		if (I18n.exists(key)) valueText.withText(I18n.get(key));
	}
}
