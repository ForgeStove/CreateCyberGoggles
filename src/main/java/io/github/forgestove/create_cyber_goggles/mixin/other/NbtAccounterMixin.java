package io.github.forgestove.create_cyber_goggles.mixin.other;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.util.SafeRun;
import net.minecraft.nbt.NbtAccounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(NbtAccounter.class)
public abstract class NbtAccounterMixin {
	@Inject(method = "accountBytes(J)V", at = @At("HEAD"), cancellable = true)
	public void accountBytes(CallbackInfo callbackInfo) {
		SafeRun.run(() -> {if (CCG.CONFIG.other.nbtFix) callbackInfo.cancel();});
	}
}
