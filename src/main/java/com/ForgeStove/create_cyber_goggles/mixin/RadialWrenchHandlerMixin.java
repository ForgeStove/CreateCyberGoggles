package com.ForgeStove.create_cyber_goggles.mixin;
import com.ForgeStove.create_cyber_goggles.Config;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.contraptions.wrench.*;
import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(RadialWrenchHandler.class) public abstract class RadialWrenchHandlerMixin {
	@Inject(method = "onKeyInput", at = @At("HEAD"), cancellable = true)
	private static void onKeyInput(int key, boolean pressed, CallbackInfo callbackInfo) {
		if (!Config.AllowEmptyHandToRotate.get()) return;
		callbackInfo.cancel();
		if (!pressed || key != AllKeys.ROTATE_MENU.getBoundCode()) return;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null) return;
		Level level = player.level();
		HitResult objectMouseOver = mc.hitResult;
		if (!(objectMouseOver instanceof BlockHitResult blockHitResult)) return;
		BlockPos blockPos = blockHitResult.getBlockPos();
		RadialWrenchMenu.tryCreateFor(level.getBlockState(blockPos), blockPos, level).ifPresent(ScreenOpener::open);
	}
}
