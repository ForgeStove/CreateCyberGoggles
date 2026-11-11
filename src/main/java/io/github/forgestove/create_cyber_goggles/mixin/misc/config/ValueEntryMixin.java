package io.github.forgestove.create_cyber_goggles.mixin.misc.config;
import io.github.forgestove.create_cyber_goggles.foundation.util.CatnipConfigLocalization;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.*;
import io.github.forgestove.flexconfig.api.ConfigTranslationKeys;
import net.createmod.catnip.config.ui.ConfigScreen;
import net.createmod.catnip.config.ui.entries.ValueEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ValueEntry.class)
public abstract class ValueEntryMixin {
	@Shadow protected ConfigValue<?> value;
	@Shadow @Final protected Type type;
	@Inject(method = "<init>", at = @At("TAIL"))
	private void localize(CallbackInfo ci) {
		var modId = ConfigScreen.modID;
		if (modId == null) return;
		var path = value.getPath();
		var key = CatnipConfigLocalization.optionKey(modId, path);
		if (key == null) return;
		var labeled = (LabeledEntryAccessor) this;
		var tooltip = labeled.getLabelTooltip();
		var header = tooltip.isEmpty() ? Component.empty() : tooltip.getFirst();
		if (I18n.exists(key)) {
			var text = I18n.get(key);
			labeled.getLabel().withText(text);
			header = Component.literal(text).withStyle(ChatFormatting.WHITE);
			if (!tooltip.isEmpty()) tooltip.set(0, header);
		}
		var tooltipKey = ConfigTranslationKeys.tooltip(key);
		if (I18n.exists(tooltipKey)) {
			var text = I18n.get(tooltipKey);
			if (!text.isBlank())
				CatnipConfigLocalization.applyTooltip(tooltip, header, text, modId, type, path, ((EntryAccessor) this).getAnnotations());
		}
		var unitKey = ConfigTranslationKeys.unit(key);
		if (I18n.exists(unitKey)) labeled.setUnit(I18n.get(unitKey));
	}
}
