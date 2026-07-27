package io.github.forgestove.create_cyber_goggles.mixin.compact;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerBlockEntity;

import java.util.List;
@Pseudo
@Mixin(value = BlazeForgerBlockEntity.class, remap = false)
public abstract class BlazeForgerBlockEntityMixin {
	@Shadow protected int processingTime;
	@Inject(method = "addToGoggleTooltip", at = @At("RETURN"))
	private void ccg$addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.goggles.enhancedInfo || processingTime <= 0) return;
		CCGLang.add(Component.translatable("create_cyber_goggles.tooltip.leftTime").withStyle(ChatFormatting.GRAY))
			.number(processingTime / 20, ChatFormatting.GOLD)
			.space()
			.seconds(ChatFormatting.GRAY)
			.forGoggles(tooltip);
	}
}
