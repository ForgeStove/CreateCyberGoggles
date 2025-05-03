package com.forgestove.create_cyber_goggles.mixin.nbt;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import net.minecraft.nbt.NbtAccounter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(NbtAccounter.class)
public abstract class NbtAccounterMixin {
	@Inject(method = "accountBytes(J)V", at = @At("HEAD"), cancellable = true)
	public void accountBytes(@NotNull CallbackInfo callbackInfo) {
		try {
			if (CreateCyberGoggles.config.nbt.nbtFix) callbackInfo.cancel();
		} catch (Exception ignored) {
		}
	}
}
