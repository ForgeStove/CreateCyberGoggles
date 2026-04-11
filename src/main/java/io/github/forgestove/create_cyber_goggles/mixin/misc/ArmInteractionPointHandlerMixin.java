package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.content.kinetics.mechanicalArm.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(value = ArmInteractionPointHandler.class, remap = false)
public class ArmInteractionPointHandlerMixin {
	@Shadow static List<ArmInteractionPoint> currentSelection;
	@Shadow static ItemStack currentItem;
	@Unique private static long ccg$rangeCacheKey = Long.MIN_VALUE;
	@Unique private static Set<BlockPos> ccg$cachedMergedRange = Set.of();
	@WrapOperation(
		method = "flushSettings", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;closerThan(Lnet/minecraft/core/Vec3i;D)Z"
	)
	)
	private static boolean flushSettings(BlockPos instance, Vec3i vector, double distance, Operation<Boolean> original) {
		return CCG.config.misc.removeMechanicalArmLimit || original.call(instance, vector, distance);
	}
	@Inject(method = "tick", at = @At("TAIL"))
	private static void ccg$renderPlacementPreviewConnections(CallbackInfo ci) {
		if (!CCG.config.outliner.betterLine) return;
		if (currentItem == null || currentSelection == null || currentSelection.isEmpty()) return;
		var mc = Minecraft.getInstance();
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
			outliner.showLine("MechanicalArmPlacementPreview_" + i, source, target).lineWidth(1 / 8f).colored(close ? 0x9ede73 : 0xff7171);
		}
		outliner.showAABB("MechanicalArmPos", getBounds(pos)).lineWidth(1 / 16f).colored(allClose ? 0x9ede73 : 0xff7171);
		if (removeLimit) return;
		var mergedRange = ccg$getMergedRange();
		if (!mergedRange.isEmpty()) outliner.showCluster("MechanicalArmConnectableRange", mergedRange)
			.lineWidth(1 / 64f)
			.withFaceTextures(AllSpecialTextures.HIGHLIGHT_CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.colored(0x9ede73);
	}
	@Unique
	private static Set<BlockPos> ccg$getMergedRange() {
		var key = ccg$selectionKey();
		if (key == ccg$rangeCacheKey) return ccg$cachedMergedRange;
		var range = ArmBlockEntity.getRange();
		Set<BlockPos> intersection = null;
		for (var point : currentSelection) {
			if (point == null || !point.isValid()) continue;
			var center = point.getPos();
			Set<BlockPos> currentSphere = new HashSet<>();
			for (var dx = -range; dx <= range; dx++)
				for (var dy = -range; dy <= range; dy++)
					for (var dz = -range; dz <= range; dz++) {
						var candidate = center.offset(dx, dy, dz);
						if (!center.closerThan(candidate, range)) continue;
						currentSphere.add(candidate);
					}
			if (intersection == null) intersection = currentSphere;
			else intersection.retainAll(currentSphere);
		}
		ccg$rangeCacheKey = key;
		ccg$cachedMergedRange = intersection == null ? Set.of() : intersection;
		return ccg$cachedMergedRange;
	}
	@Unique
	private static long ccg$selectionKey() {
		long key = ArmBlockEntity.getRange();
		for (var point : currentSelection) {
			if (point == null || !point.isValid()) continue;
			key = key * 31L + point.getPos().asLong();
		}
		return key;
	}
}
