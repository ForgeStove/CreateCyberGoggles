package io.github.forgestove.create_cyber_goggles.mixin.chainConveyor;
import com.simibubi.create.content.equipment.armor.CardboardArmorHandler;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRidingHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(value = CardboardArmorHandler.class, remap = false)
public abstract class CardboardArmorHandlerMixin {
	@Inject(method = "testForStealth", at = @At("HEAD"), cancellable = true)
	private static void injectTestForStealth(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.CONFIG.chainConveyor.cardBoardedYourself) return;
		if (isServer()) return;
		if (ChainConveyorRidingHandler.ridingChainConveyor == null) return;
		if (!(entityIn instanceof LocalPlayer)) return;
		if (!testForStealth()) return;
		cir.setReturnValue(true);
	}
}
