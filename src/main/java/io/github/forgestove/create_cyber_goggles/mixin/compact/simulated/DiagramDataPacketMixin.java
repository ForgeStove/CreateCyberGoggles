package io.github.forgestove.create_cyber_goggles.mixin.compact.simulated;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.content.forceOverlay.ForceDataCache;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Pseudo
@Mixin(DiagramDataPacket.class)
public abstract class DiagramDataPacketMixin {
	@Inject(method = "handle(Ldev/simulated_team/simulated/network/packets/contraption_diagram/DiagramDataPacket;)V", at = @At("TAIL"))
	private static void handle(DiagramDataPacket packet, CallbackInfo ci) {
		if (!CCG.config.aeronautics.forceOverlay.enableForceOverlay) return;
		ForceDataCache.set(packet);
	}
}
