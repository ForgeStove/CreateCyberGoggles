package com.ForgeStove.create_cyber_goggles.mixin;
import com.ForgeStove.create_cyber_goggles.Config;
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
@Mixin(ChainConveyorRidingHandler.class) public abstract class ChainConveyorRidingHandlerMixin {
	@Inject(method = "clientTick", at = @At("HEAD"), cancellable = true)
	private static void clientTick(CallbackInfo callbackInfo) {
		if (!Config.AllowEmptyHandToRideChainConveyor.get()) return;
		callbackInfo.cancel();
		if (ChainConveyorRidingHandler.ridingChainConveyor == null) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.isPaused()) return;
		LocalPlayer localPlayer = mc.player;
		if (localPlayer == null) return;
		ClientLevel clientLevel = mc.level;
		if (clientLevel == null) return;
		BlockEntity blockEntity = clientLevel.getBlockEntity(ChainConveyorRidingHandler.ridingChainConveyor);
		if (localPlayer.isShiftKeyDown()
				|| !(blockEntity instanceof ChainConveyorBlockEntity chainConveyorBlockEntity)
				|| ChainConveyorRidingHandler.ridingConnection != null
				&& !chainConveyorBlockEntity.connections.contains(ChainConveyorRidingHandler.ridingConnection)) {
			stopRiding();
			return;
		}
		chainConveyorBlockEntity.prepareStats();
		Vec3 playerPosition = localPlayer.position().add(0, localPlayer.getBoundingBox().getYsize() + 0.5, 0);
		updateTargetPosition(mc, chainConveyorBlockEntity);
		blockEntity = clientLevel.getBlockEntity(ChainConveyorRidingHandler.ridingChainConveyor);
		if (!(blockEntity instanceof ChainConveyorBlockEntity)) return;
		chainConveyorBlockEntity = (ChainConveyorBlockEntity) blockEntity;
		chainConveyorBlockEntity.prepareStats();
		Vec3 targetPosition;
		if (ChainConveyorRidingHandler.ridingConnection != null) {
			ChainConveyorBlockEntity.ConnectionStats stats = chainConveyorBlockEntity.connectionStats.get(
					ChainConveyorRidingHandler.ridingConnection);
			targetPosition = stats.start().add((
					stats.end().subtract(stats.start())
			).normalize().scale(Math.min(stats.chainLength(), ChainConveyorRidingHandler.chainPosition)));
		} else targetPosition = Vec3.atBottomCenterOf(ChainConveyorRidingHandler.ridingChainConveyor)
				.add(VecHelper.rotate(
						new Vec3(0, 0.25, 1),
						ChainConveyorRidingHandler.chainPosition,
						Direction.Axis.Y
				));
		localPlayer.setDeltaMovement(localPlayer.getDeltaMovement()
				.scale(0.75)
				.add(targetPosition.subtract(playerPosition).scale(0.25)));
		if (AnimationTickHolder.getTicks() % 10 == 0)
			CatnipServices.NETWORK.sendToServer(new ServerboundChainConveyorRidingPacket(
					ChainConveyorRidingHandler.ridingChainConveyor,
					false
			));
	}
	@Shadow private static void stopRiding() {
	}
	@Shadow private static void updateTargetPosition(Minecraft mc, ChainConveyorBlockEntity chainConveyorBlockEntity) {
	}
}
