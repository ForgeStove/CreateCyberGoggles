package com.forgestove.create_cyber_goggles.mixin.goggles;
import com.forgestove.create_cyber_goggles.config.Config;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(GogglesItem.class)
public abstract class GogglesItemMixin {
	@Inject(method = "isWearingGoggles", at = @At("HEAD"), remap = false, cancellable = true)
	private static void isWearingGoggles(CallbackInfoReturnable<Boolean> returnable) {
		var gameMode = Minecraft.getInstance().gameMode;
		if (gameMode == null) return;
		if (!switch (gameMode.getPlayerMode()) {
			case SURVIVAL -> Config.data.goggles.enableInSurvival;
			case CREATIVE -> Config.data.goggles.enableInCreative;
			case SPECTATOR -> Config.data.goggles.enableInSpectator;
			case ADVENTURE -> Config.data.goggles.enableInAdventure;
		}) return;
		returnable.setReturnValue(true);
	}
}
