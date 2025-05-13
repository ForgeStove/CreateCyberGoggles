package com.forgestove.create_cyber_goggles.mixin.armor;
import com.forgestove.create_cyber_goggles.content.util.Common;
import com.simibubi.create.content.equipment.armor.CardboardArmorHandler;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRidingHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(CardboardArmorHandler.class)
public abstract class CardboardArmorHandlerMixin {
	@Inject(method = "testForStealth", at = @At("HEAD"), remap = false, cancellable = true)
	private static void testForStealth(Entity entityIn, CallbackInfoReturnable<Boolean> returnable) {
		if (ChainConveyorRidingHandler.ridingChainConveyor == null) return;
		if (!(entityIn instanceof LocalPlayer player)) return;
		if (!Common.testForStealth(player)) return;
		returnable.setReturnValue(true);
	}
}
