package io.github.forgestove.create_cyber_goggles.mixin.simulated;
import dev.simulated_team.simulated.content.entities.honey_glue.HoneyGlueClientHandler;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(HoneyGlueClientHandler.class)
public abstract class HoneyGlueClientHandlerMixin {
	@Inject(method = "getHoneyGlueHand", at = @At("HEAD"), cancellable = true)
	private void getHoneyGlueHand(Player player, CallbackInfoReturnable<InteractionHand> cir) {
		if (!CCGKey.showHoneyGlue.isDown()) return;
		cir.setReturnValue(player.getMainHandItem().has(DataComponents.DAMAGE) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
	}
}
