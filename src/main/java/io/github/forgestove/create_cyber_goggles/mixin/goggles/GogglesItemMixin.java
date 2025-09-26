package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value = GogglesItem.class, remap = false)
public abstract class GogglesItemMixin {
	@Inject(method = "isWearingGoggles", at = @At("HEAD"), cancellable = true)
	private static void isWearingGoggles(CallbackInfoReturnable<Boolean> returnable) {
		var mc = Minecraft.getInstance();
		if (mc.gameMode == null) return;
		var mode = CCG.CONFIG.gameMode;
		if (mode.enableGoggle && !switch (mc.gameMode.getPlayerMode()) {
			case SURVIVAL -> mode.enableInSurvival;
			case CREATIVE -> mode.enableInCreative;
			case SPECTATOR -> mode.enableInSpectator;
			case ADVENTURE -> mode.enableInAdventure;
		}) return;
		returnable.setReturnValue(true);
	}
}
