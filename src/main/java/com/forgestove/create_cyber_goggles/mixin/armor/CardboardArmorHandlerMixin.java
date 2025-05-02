package com.forgestove.create_cyber_goggles.mixin.armor;
import com.forgestove.create_cyber_goggles.Util;
import com.simibubi.create.content.equipment.armor.CardboardArmorHandler;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRidingHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(CardboardArmorHandler.class)
public abstract class CardboardArmorHandlerMixin {
	@Inject(method = "testForStealth", at = @At("HEAD"), cancellable = true)
	private static void testForStealth(Entity entityIn, CallbackInfoReturnable<Boolean> returnable) {
		if (ChainConveyorRidingHandler.ridingChainConveyor == null || !(entityIn instanceof LocalPlayer player) || !Util.testForStealth(
			player)) return;
		returnable.setReturnValue(true);
	}
}
