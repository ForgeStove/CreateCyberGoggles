package com.forgestove.create_cyber_goggles.mixin.goggles;
import com.forgestove.create_cyber_goggles.CCG;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(GogglesItem.class)
public abstract class GogglesItemMixin {
	@Inject(method = "isWearingGoggles", at = @At("HEAD"), cancellable = true)
	private static void isWearingGoggles(CallbackInfoReturnable<Boolean> returnable) {
		var mc = Minecraft.getInstance();
		if (mc.gameMode == null) return;
		var gameMode = CCG.CONFIG.gameMode;
		if (!switch (mc.gameMode.getPlayerMode()) {
			case SURVIVAL -> gameMode.enableInSurvival;
			case CREATIVE -> gameMode.enableInCreative;
			case SPECTATOR -> gameMode.enableInSpectator;
			case ADVENTURE -> gameMode.enableInAdventure;
		}) return;
		returnable.setReturnValue(true);
	}
}
