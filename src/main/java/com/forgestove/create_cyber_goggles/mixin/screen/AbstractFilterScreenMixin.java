package com.forgestove.create_cyber_goggles.mixin.screen;
import com.simibubi.create.content.logistics.filter.AbstractFilterScreen;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(AbstractFilterScreen.class)
public abstract class AbstractFilterScreenMixin {
	@Redirect(
		method = "containerTick",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;closeContainer()V"),
		remap = false
	)
	private void containerTick(Player instance) {}
}
