package io.github.forgestove.create_cyber_goggles.mixin.chainConveyor;
import com.simibubi.create.content.equipment.armor.CardboardArmorHandler;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRidingHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.testForStealth;
@Mixin(value = CardboardArmorHandler.class, remap = false)
public abstract class CardboardArmorHandlerMixin {
	@Inject(method = "testForStealth", at = @At("HEAD"), cancellable = true)
	private static void injectTestForStealth(Entity entityIn, CallbackInfoReturnable<Boolean> returnable) {
		if (ChainConveyorRidingHandler.ridingChainConveyor == null) return;
		if (!(entityIn instanceof LocalPlayer)) return;
		if (!testForStealth()) return;
		returnable.setReturnValue(true);
	}
}
