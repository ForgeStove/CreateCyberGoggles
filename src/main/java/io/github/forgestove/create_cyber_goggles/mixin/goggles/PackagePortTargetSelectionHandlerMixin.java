package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.content.logistics.packagePort.PackagePortTargetSelectionHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PackagePortTargetSelectionHandler.class)
public abstract class PackagePortTargetSelectionHandlerMixin {
	@Inject(method = "animateConnection", at = @At("HEAD"), cancellable = true)
	private static void animateConnection(Minecraft mc, Vec3 source, Vec3 target, Color color, CallbackInfo callbackInfo) {
		if (!CCG.CONFIG.goggles.betterLine) return;
		callbackInfo.cancel();
		Outliner.getInstance().showLine("PackagePortConnection", source, target).lineWidth(1 / 8f).colored(color);
	}
}
