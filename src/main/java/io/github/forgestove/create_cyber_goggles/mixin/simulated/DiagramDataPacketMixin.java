package io.github.forgestove.create_cyber_goggles.mixin.simulated;

import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import io.github.forgestove.create_cyber_goggles.core.overlay.ForceDataCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts incoming {@link DiagramDataPacket} to cache force data for
 * the in-world overlay, regardless of whether the diagram screen is open.
 */
@Mixin(DiagramDataPacket.class)
public abstract class DiagramDataPacketMixin {

    @Inject(
            method = "handle(Ldev/simulated_team/simulated/network/packets/contraption_diagram/DiagramDataPacket;)V",
            at = @At("TAIL"),
            remap = false
    )
    private static void onHandle(final DiagramDataPacket packet, final CallbackInfo ci) {
        ForceDataCache.set(packet.forces(), packet.mass());
    }
}
