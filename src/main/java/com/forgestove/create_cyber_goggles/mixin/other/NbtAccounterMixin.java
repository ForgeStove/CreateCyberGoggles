package com.forgestove.create_cyber_goggles.mixin.other;
import com.forgestove.create_cyber_goggles.content.config.*;
import com.forgestove.create_cyber_goggles.content.util.SafeRun;
import net.minecraft.nbt.NbtAccounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(NbtAccounter.class)
public abstract class NbtAccounterMixin {
	@Inject(method = "accountBytes(J)V", at = @At("HEAD"), cancellable = true)
	public void accountBytes(CallbackInfo callbackInfo) {
		SafeRun.run(() -> {
			if (CCGConfig.config.other.nbtFix) callbackInfo.cancel();});
	}
}
