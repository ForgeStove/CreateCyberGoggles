package io.github.forgestove.create_cyber_goggles.mixin.simulated;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import io.github.forgestove.create_cyber_goggles.core.event.forceOverlay.ForceDataCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/**
 * 拦截接收到的 {@link DiagramDataPacket}，将力数据缓存供世界内覆盖层使用，
 * 无论图解界面是否打开。
 */
@Mixin(DiagramDataPacket.class)
public abstract class DiagramDataPacketMixin {
	@Inject(
		method = "handle(Ldev/simulated_team/simulated/network/packets/contraption_diagram/DiagramDataPacket;)V",
		at = @At("TAIL"),
		remap = false
	)
	private static void handle(DiagramDataPacket packet, CallbackInfo ci) {
		ForceDataCache.set(packet);
	}
}
