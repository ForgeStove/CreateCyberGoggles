package io.github.forgestove.create_cyber_goggles.mixin.misc;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import io.github.forgestove.create_cyber_goggles.core.util.EnderChestTooltipUtil;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerEnderChestContainer.class)
public abstract class PlayerEnderChestContainerMixin implements Self<PlayerEnderChestContainer> {
	@Inject(method = "startOpen", at = @At("TAIL"), require = 0)
	private void startOpen(ContainerUser user, CallbackInfo ci) {
		EnderChestTooltipUtil.capture(thiz());
	}
	@Inject(method = "stopOpen", at = @At("TAIL"), require = 0)
	private void stopOpen(ContainerUser user, CallbackInfo ci) {
		EnderChestTooltipUtil.capture(thiz());
	}
}
