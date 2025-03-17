package com.ForgeStove.create_cyber_goggles.mixin.goggles;
import com.ForgeStove.create_cyber_goggles.config.*;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(GogglesItem.class) public abstract class GogglesItemMixin {
	@Inject(method = "isWearingGoggles", at = @At("HEAD"), cancellable = true)
	private static void isWearingGoggles(CallbackInfoReturnable<Boolean> returnable) {
		MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
		if (gameMode == null) return;
		switch (gameMode.getPlayerMode()) {
			case SURVIVAL -> {
				if (Config.enableInSurvival.get()) returnable.setReturnValue(true);
			}
			case CREATIVE -> {
				if (Config.enableInCreative.get()) returnable.setReturnValue(true);
			}
			case SPECTATOR -> {
				if (Config.enableInSpectator.get()) returnable.setReturnValue(true);
			}
			case ADVENTURE -> {
				if (Config.enableInAdventure.get()) returnable.setReturnValue(true);
			}
		}
	}
}
