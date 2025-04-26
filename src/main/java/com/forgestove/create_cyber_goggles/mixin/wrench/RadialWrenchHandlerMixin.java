package com.forgestove.create_cyber_goggles.mixin.wrench;
import com.forgestove.create_cyber_goggles.config.Config;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.contraptions.wrench.*;
import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(RadialWrenchHandler.class)
public abstract class RadialWrenchHandlerMixin {
	@Inject(method = "onKeyInput", at = @At("HEAD"), remap = false, cancellable = true)
	private static void onKeyInput(int key, boolean pressed, CallbackInfo callbackInfo) {
		if (!Config.data.wrench.alwaysAllowRotating) return;
		callbackInfo.cancel();
		if (!pressed || key != AllKeys.ROTATE_MENU.getBoundCode()) return;
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null) return;
		var level = player.level();
		var hitResult = mc.hitResult;
		if (!(hitResult instanceof BlockHitResult blockHitResult)) return;
		var blockPos = blockHitResult.getBlockPos();
		RadialWrenchMenu.tryCreateFor(level.getBlockState(blockPos), blockPos, level).ifPresent(ScreenOpener::open);
	}
}
