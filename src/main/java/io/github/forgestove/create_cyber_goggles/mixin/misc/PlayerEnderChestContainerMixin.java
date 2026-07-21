package io.github.forgestove.create_cyber_goggles.mixin.misc;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import io.github.forgestove.create_cyber_goggles.core.util.EnderChestTooltipUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerEnderChestContainer.class)
public abstract class PlayerEnderChestContainerMixin implements Self<PlayerEnderChestContainer> {
	@Inject(method = "startOpen", at = @At("TAIL"))
	private void startOpen(Player player, CallbackInfo ci) {
		EnderChestTooltipUtil.capture(thiz());
	}
	@Inject(method = "stopOpen", at = @At("TAIL"))
	private void stopOpen(Player player, CallbackInfo ci) {
		EnderChestTooltipUtil.capture(thiz());
	}
}
