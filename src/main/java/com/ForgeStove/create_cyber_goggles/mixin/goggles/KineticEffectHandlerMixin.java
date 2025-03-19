package com.ForgeStove.create_cyber_goggles.mixin.goggles;
import com.ForgeStove.create_cyber_goggles.Config;
import com.simibubi.create.content.kinetics.base.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(KineticEffectHandler.class) public abstract class KineticEffectHandlerMixin {
	@Shadow KineticBlockEntity kte;
	@Inject(method = "tick", at = @At("HEAD")) private void tick(CallbackInfo callbackInfo) {
		if (!Config.enableKineticEffect.get()) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.isPaused() || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
		if (blockHitResult.getType() == HitResult.Type.MISS) return;
		if (!blockHitResult.getBlockPos().equals(kte.getBlockPos())) return;
		spawnRotationIndicators();
	}
	@Shadow public void spawnRotationIndicators() {
	}
}
