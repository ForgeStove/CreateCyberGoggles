package com.ForgeStove.create_cyber_goggles.mixin.goggles;
import com.ForgeStove.create_cyber_goggles.config.Configs;
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
				if (Configs.client().enableOnSurvival.get()) returnable.setReturnValue(true);
			}
			case CREATIVE -> {
				if (Configs.client().enableOnCreative.get()) returnable.setReturnValue(true);
			}
			case SPECTATOR -> {
				if (Configs.client().enableOnSpectator.get()) returnable.setReturnValue(true);
			}
			case ADVENTURE -> {
				if (Configs.client().enableOnAdventure.get()) returnable.setReturnValue(true);
			}
		}
	}
}
