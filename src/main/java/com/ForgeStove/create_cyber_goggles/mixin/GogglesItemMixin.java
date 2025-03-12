package com.ForgeStove.create_cyber_goggles.mixin;
import com.ForgeStove.create_cyber_goggles.Config;
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
		if (gameMode != null) switch (gameMode.getPlayerMode()) {
			case SURVIVAL:
				if (Config.EnableGogglesWhenSurvival.get()) returnable.setReturnValue(true);
				break;
			case CREATIVE:
				if (Config.EnableGogglesWhenCreative.get()) returnable.setReturnValue(true);
				break;
			case SPECTATOR:
				if (Config.EnableGogglesWhenSpectator.get()) returnable.setReturnValue(true);
				break;
			case ADVENTURE:
				if (Config.EnableGogglesWhenAdventure.get()) returnable.setReturnValue(true);
				break;
		}
	}
}
