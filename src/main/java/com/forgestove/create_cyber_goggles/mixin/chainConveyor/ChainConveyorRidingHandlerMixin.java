package com.forgestove.create_cyber_goggles.mixin.chainConveyor;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import com.forgestove.create_cyber_goggles.content.Util;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.kinetics.chainConveyor.*;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRidingHandler.*;
@Mixin(ChainConveyorRidingHandler.class)
public abstract class ChainConveyorRidingHandlerMixin {
	@Inject(method = "clientTick", at = @At("HEAD"), cancellable = true)
	private static void clientTick(CallbackInfo callbackInfo) {
		callbackInfo.cancel();
		if (ridingChainConveyor == null) return;
		var mc = Minecraft.getInstance();
		if (mc.isPaused()) return;
		var player = mc.player;
		if (player == null) return;
		var chainConveyor = CreateCyberGoggles.config.chainConveyor;
		if (!chainConveyor.alwaysAllowRiding && !AllItemTags.CHAIN_RIDEABLE.matches(mc.player.getMainHandItem())) {
			stopRiding();
			return;
		}
		var level = mc.level;
		if (level == null) return;
		var blockEntity = level.getBlockEntity(ridingChainConveyor);
		if (player.isShiftKeyDown()
			|| !(blockEntity instanceof ChainConveyorBlockEntity chainConveyorBlockEntity)
			|| ridingConnection != null && !chainConveyorBlockEntity.connections.contains(ridingConnection)) {
			stopRiding();
			return;
		}
		chainConveyorBlockEntity.prepareStats();
		var chainYOffset = 0.5f * mc.player.getScale();
		var playerPosition = mc.player.position().add(0, mc.player.getBoundingBox().getYsize() + chainYOffset, 0);
		updateTargetPosition(mc, chainConveyorBlockEntity);
		blockEntity = level.getBlockEntity(ridingChainConveyor);
		if (!(blockEntity instanceof ChainConveyorBlockEntity)) return;
		chainConveyorBlockEntity = (ChainConveyorBlockEntity) blockEntity;
		chainConveyorBlockEntity.prepareStats();
		Vec3 targetPosition;
		if (ridingConnection != null) {
			var stats = chainConveyorBlockEntity.connectionStats.get(ridingConnection);
			targetPosition = stats.start().add((
				stats.end().subtract(stats.start())
			).normalize().scale(Math.min(stats.chainLength(), chainPosition)));
		} else
			targetPosition = Vec3.atBottomCenterOf(ridingChainConveyor).add(VecHelper.rotate(new Vec3(0, 0.25, 1), chainPosition, Axis.Y));
		if (catchingUp > 0) catchingUp--;
		var diff = targetPosition.subtract(playerPosition);
		if (catchingUp == 0 && !chainConveyor.preventFalling && (
			diff.length() > chainConveyor.separationDistance || diff.y < chainConveyor.separationHeight
		)) {
			stopRiding();
			return;
		}
		player.setDeltaMovement(player.getDeltaMovement().scale(0.75).add(diff.scale(0.25)));
		if (Util.testForStealth(player)) player.connection.send(new ServerboundPlayerCommandPacket(player, Action.PRESS_SHIFT_KEY));
		if (AnimationTickHolder.getTicks() % 10 == 0)
			CatnipServices.NETWORK.sendToServer(new ServerboundChainConveyorRidingPacket(ridingChainConveyor, false));
	}
	@Invoker("stopRiding")
	public static void stopRiding() {}
	@Shadow
	private static void updateTargetPosition(Minecraft mc, ChainConveyorBlockEntity chainConveyorBlockEntity) {}
}
