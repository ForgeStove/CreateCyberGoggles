package com.ForgeStove.create_project.mixin;
import com.simibubi.create.content.kinetics.chainConveyor.*;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRidingHandler.*;
@Mixin(ChainConveyorRidingHandler.class) public abstract class ChainConveyorRidingHandlerMixin {
	@Inject(method = "clientTick", at = @At("HEAD"), cancellable = true)
	private static void clientTick(CallbackInfo callbackInfo) {
		callbackInfo.cancel();
		if (ridingChainConveyor == null) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.isPaused()) return;
		LocalPlayer localPlayer = mc.player;
		if (localPlayer == null) return;
		ClientLevel clientLevel = mc.level;
		if (clientLevel == null) return;
		BlockEntity blockEntity = clientLevel.getBlockEntity(ridingChainConveyor);
		if (localPlayer.isShiftKeyDown()
				|| !(blockEntity instanceof ChainConveyorBlockEntity chainConveyorBlockEntity)
				|| ridingConnection != null && !chainConveyorBlockEntity.connections.contains(ridingConnection)) {
			stopRiding();
			return;
		}
		chainConveyorBlockEntity.prepareStats();
		Vec3 playerPosition = localPlayer.position().add(0, localPlayer.getBoundingBox().getYsize() + 0.5, 0);
		updateTargetPosition(mc, chainConveyorBlockEntity);
		blockEntity = clientLevel.getBlockEntity(ridingChainConveyor);
		if (!(blockEntity instanceof ChainConveyorBlockEntity)) return;
		chainConveyorBlockEntity = (ChainConveyorBlockEntity) blockEntity;
		chainConveyorBlockEntity.prepareStats();
		Vec3 targetPosition;
		if (ridingConnection != null) {
			ChainConveyorBlockEntity.ConnectionStats stats = chainConveyorBlockEntity.connectionStats.get(
					ridingConnection);
			targetPosition = stats.start().add((
					stats.end().subtract(stats.start())
			).normalize().scale(Math.min(stats.chainLength(), chainPosition)));
		} else targetPosition = Vec3.atBottomCenterOf(ridingChainConveyor)
				.add(VecHelper.rotate(new Vec3(0, 0.25, 1), chainPosition, Direction.Axis.Y));
		localPlayer.setDeltaMovement(localPlayer.getDeltaMovement()
				.scale(0.75)
				.add(targetPosition.subtract(playerPosition).scale(0.25)));
		if (AnimationTickHolder.getTicks() % 10 == 0)
			CatnipServices.NETWORK.sendToServer(new ServerboundChainConveyorRidingPacket(ridingChainConveyor, false));
	}
	@Shadow private static void stopRiding() {
	}
	@Shadow private static void updateTargetPosition(Minecraft mc, ChainConveyorBlockEntity chainConveyorBlockEntity) {
	}
}
