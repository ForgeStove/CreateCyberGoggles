package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.content.logistics.packagePort.*;
import com.simibubi.create.infrastructure.config.AllConfigs;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.HitResult.Type;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(value = PackagePortTargetSelectionHandler.class, remap = false)
public abstract class PackagePortTargetSelectionHandlerMixin {
	@Shadow public static PackagePortTarget activePackageTarget;
	@Shadow public static Vec3 exactPositionOfTarget;
	@Unique private static long ccg$rangeCacheKey = Long.MIN_VALUE;
	@Unique private static Couple<List<BlockPos>> ccg$cachedRangeHints = Couple.create(ArrayList::new);
	@Inject(method = "animateConnection", at = @At("HEAD"), cancellable = true)
	private static void animateConnection(Minecraft mc, Vec3 source, Vec3 target, Color color, CallbackInfo ci) {
		if (!CCG.config.outliner.betterLine) return;
		ci.cancel();
		outliner.showLine("PackagePortConnectionAnimated", source, target).lineWidth(1 / 8f).colored(color);
	}
	@Inject(method = "tick", at = @At("TAIL"))
	private static void ccg$renderPlacementRangeHints(CallbackInfo ci) {
		if (!CCG.config.outliner.betterLine) return;
		if (mc.level == null || activePackageTarget == null || exactPositionOfTarget == null) return;
		if (!(mc.hitResult instanceof BlockHitResult blockHit) || blockHit.getType() == Type.MISS) return;
		var pos = blockHit.getBlockPos();
		if (!mc.level.getBlockState(pos).canBeReplaced()) pos = pos.relative(blockHit.getDirection());
		var hints = ccg$getRangeHints(exactPositionOfTarget, pos.getY());
		if (hints == null) return;
		outliner.showCluster("PackagePortConnectableRange", hints.getFirst())
			.withFaceTexture(AllSpecialTextures.THIN_CHECKERED)
			.colored(0x9ede73)
			.lineWidth(0);
		outliner.showCluster("PackagePortNonConnectableRange", hints.getSecond())
			.withFaceTexture(AllSpecialTextures.THIN_CHECKERED)
			.colored(0xff7171)
			.lineWidth(0);
	}
	@Unique
	private static Couple<List<BlockPos>> ccg$getRangeHints(Vec3 target, int yLevel) {
		if (target == null) return null;
		int range = AllConfigs.server().logistics.packagePortRange.get();
		var renderY = yLevel - 1;
		var bitsX = Double.doubleToLongBits(target.x);
		var bitsY = Double.doubleToLongBits(target.y);
		var bitsZ = Double.doubleToLongBits(target.z);
		var key = 31L * (31L * (31L + bitsX) + bitsY) + bitsZ;
		key = 31L * key + yLevel;
		key = 31L * key + renderY;
		key = 31L * key + Double.doubleToLongBits(range);
		if (key == ccg$rangeCacheKey) return ccg$cachedRangeHints;
		var minX = (int) Math.floor(target.x - range);
		var maxX = (int) Math.ceil(target.x + range);
		var minZ = (int) Math.floor(target.z - range);
		var maxZ = (int) Math.ceil(target.z + range);
		Couple<List<BlockPos>> hints = Couple.create(ArrayList::new);
		for (var x = minX; x <= maxX; x++)
			for (var z = minZ; z <= maxZ; z++) {
				var candidate = new BlockPos(x, yLevel, z);
				var renderPos = new BlockPos(x, renderY, z);
				var connectable = PackagePortTargetSelectionHandler.validateDiff(target, candidate) == null;
				hints.get(connectable).add(renderPos);
			}
		ccg$rangeCacheKey = key;
		ccg$cachedRangeHints = hints;
		return ccg$cachedRangeHints;
	}
}
