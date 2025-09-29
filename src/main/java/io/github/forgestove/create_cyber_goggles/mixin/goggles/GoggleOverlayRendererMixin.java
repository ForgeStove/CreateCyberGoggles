package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.outliner.Outliner.OutlineEntry;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.isInGame;
@Mixin(GoggleOverlayRenderer.class)
public abstract class GoggleOverlayRendererMixin {
	@Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
	private static void renderOverlay(CallbackInfo callbackInfo) {
		if (!CCG.CONFIG.goggles.disableScreenGoggles || isInGame()) return;
		callbackInfo.cancel();
	}
	@WrapOperation(
		method = "renderOverlay", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPlayerMode()Lnet/minecraft/world/level/GameType;"
	)
	)
	private static GameType wrapGameMode(MultiPlayerGameMode instance, Operation<GameType> original) {
		return CCG.CONFIG.gameMode.enableInSpectator ? null : original.call(instance);
	}
	@WrapOperation(
		method = "renderOverlay", at = @At(
		value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"
	)
	)
	private static Collection<OutlineEntry> wrapCollection(
		Map<Object, OutlineEntry> instance,
		Operation<Collection<OutlineEntry>> original
	) {
		return CCG.CONFIG.goggles.canRenderOnValueBox ? Collections.emptyList() : original.call(instance);
	}
}
