package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.client.content.kinetics.mechanicalArm.ArmInteractionPointHandler;
import com.zurrtum.create.content.kinetics.mechanicalArm.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.HitResult.Type;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.outliner;
@Mixin(ArmInteractionPointHandler.class)
public class ArmInteractionPointHandlerMixin {
	@Shadow static List<ArmInteractionPoint> currentSelection;
	@Shadow static ItemStack currentItem;
	@Unique private static long ccg$rangeCacheKey = Long.MIN_VALUE;
	@Unique private static Couple<List<BlockPos>> ccg$cachedRangeHints = Couple.create(ArrayList::new);
	@WrapOperation(
		method = "flushSettings", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;closerThan(Lnet/minecraft/core/Vec3i;D)Z"
	)
	)
	private static boolean flushSettings(BlockPos instance, Vec3i vector, double distance, Operation<Boolean> original) {
		return CCG.config.misc.removeMechanicalArmLimit || original.call(instance, vector, distance);
	}
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V"))
	private static void ccg$preventSelectionDiscardOnArmSwitch(List<?> instance, Operation<Void> original) {
		if (CCG.config.misc.preventSelectionDiscard) return;
		original.call(instance);
	}
	@Inject(method = "tick", at = @At("TAIL"))
	private static void ccg$renderPlacementPreviewConnections(Minecraft mc, CallbackInfo ci) {
		if (!CCG.config.outliner.betterLine) return;
		if (currentItem != null && currentSelection != null && !currentSelection.isEmpty()) {
			var player = mc.player;
			var hit = mc.hitResult;
			if (player != null && player.isShiftKeyDown() && (hit == null || hit.getType() == Type.MISS) && mc.options.keyUse.isDown()) {
				player.swing(InteractionHand.MAIN_HAND);
				currentSelection.clear();
				return;
			}
		}
		if (currentItem == null || currentSelection == null || currentSelection.isEmpty()) return;
		var hit = mc.hitResult;
		if (!(hit instanceof BlockHitResult blockHit)) return;
		if (mc.level == null) return;
		var pos = blockHit.getBlockPos();
		if (!mc.level.getBlockState(pos).canBeReplaced()) pos = pos.relative(blockHit.getDirection());
		var source = Vec3.atCenterOf(pos);
		var allClose = true;
		var removeLimit = CCG.config.misc.removeMechanicalArmLimit;
		for (var i = 0; i < currentSelection.size(); i++) {
			var point = currentSelection.get(i);
			if (point == null || !point.isValid()) continue;
			var target = Vec3.atCenterOf(point.getPos());
			var close = removeLimit || point.getPos().closerThan(pos, ArmBlockEntity.getRange());
			if (!close) allClose = false;
			outliner.showLine("MechanicalArmPlacementPreview" + i, source, target).lineWidth(1 / 8f).colored(close ? 0x9ede73 : 0xff7171);
		}
		if (mc.level.getBlockState(pos).canBeReplaced())
			outliner.chaseAABB("MechanicalArmPos", new AABB(pos).contract(0, 1, 0).deflate(0.125, 0, 0.125))
				.lineWidth(1 / 16f)
				.colored(allClose ? 0x9ede73 : 0xff7171);
		if (removeLimit) return;
		var hints = ccg$getRangeHints(pos.getY() - 1);
		if (hints == null) return;
		outliner.showCluster("MechanicalArmConnectableRange", hints.getFirst())
			.withFaceTexture(AllSpecialTextures.THIN_CHECKERED)
			.colored(0x9ede73)
			.lineWidth(0);
		outliner.showCluster("MechanicalArmNonConnectableRange", hints.getSecond())
			.withFaceTexture(AllSpecialTextures.THIN_CHECKERED)
			.colored(0xff7171)
			.lineWidth(0);
	}
	@Unique
	private static Couple<List<BlockPos>> ccg$getRangeHints(int yLevel) {
		var key = ArmBlockEntity.getRange() * 31L + yLevel;
		for (var point : currentSelection) {
			if (point == null || !point.isValid()) continue;
			key = key * 31L + point.getPos().asLong();
		}
		if (key == ccg$rangeCacheKey) return ccg$cachedRangeHints;
		var range = ArmBlockEntity.getRange();
		var minX = Integer.MAX_VALUE;
		var maxX = Integer.MIN_VALUE;
		var minZ = Integer.MAX_VALUE;
		var maxZ = Integer.MIN_VALUE;
		var hasValidPoint = false;
		for (var point : currentSelection) {
			if (point == null || !point.isValid()) continue;
			hasValidPoint = true;
			var center = point.getPos();
			minX = Math.min(minX, center.getX() - range);
			maxX = Math.max(maxX, center.getX() + range);
			minZ = Math.min(minZ, center.getZ() - range);
			maxZ = Math.max(maxZ, center.getZ() + range);
		}
		Couple<List<BlockPos>> hints = Couple.create(ArrayList::new);
		if (hasValidPoint) {
			var rangeSq = range * range;
			for (var x = minX; x <= maxX; x++)
				for (var z = minZ; z <= maxZ; z++) {
					var candidate = new BlockPos(x, yLevel, z);
					var connectable = true;
					for (var point : currentSelection) {
						if (point == null || !point.isValid()) continue;
						var center = point.getPos();
						var dx = x - center.getX();
						var dy = yLevel - center.getY();
						var dz = z - center.getZ();
						if (dx * dx + dy * dy + dz * dz >= rangeSq) {
							connectable = false;
							break;
						}
					}
					hints.get(connectable).add(candidate);
				}
		}
		ccg$rangeCacheKey = key;
		ccg$cachedRangeHints = hints;
		return ccg$cachedRangeHints;
	}
}
