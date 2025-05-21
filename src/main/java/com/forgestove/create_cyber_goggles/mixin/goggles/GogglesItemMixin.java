package com.forgestove.create_cyber_goggles.mixin.goggles;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(GogglesItem.class)
public abstract class GogglesItemMixin {
	@Inject(method = "isWearingGoggles", at = @At("HEAD"), remap = false, cancellable = true)
	private static void isWearingGoggles(CallbackInfoReturnable<Boolean> returnable) {
		var mc = Minecraft.getInstance();
		if (mc.screen != null) {
			returnable.setReturnValue(false);
			return;
		}
		if (mc.gameMode == null) return;
		var goggles = CCGConfig.config.goggles;
		if (!switch (mc.gameMode.getPlayerMode()) {
			case SURVIVAL -> goggles.enableInSurvival;
			case CREATIVE -> goggles.enableInCreative;
			case SPECTATOR -> goggles.enableInSpectator;
			case ADVENTURE -> goggles.enableInAdventure;
		}) return;
		returnable.setReturnValue(true);
	}
}
