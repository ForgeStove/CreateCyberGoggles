package io.github.forgestove.create_cyber_goggles.core.event;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.content.kinetics.fan.*;
import com.zurrtum.create.content.kinetics.mechanicalArm.*;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity;
import com.zurrtum.create.content.logistics.packagePort.PackagePortBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.AirCurrentUtil;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.NozzleBlockEntityAccessor;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class DelayRender {
	public static final Object2IntOpenHashMap<BlockEntity> cachedBE = new Object2IntOpenHashMap<>();
	public static void tick(Minecraft mc) {
		if (!CCG.CONFIG.outliner.renderAnalogBox) return;
		if (mc.level == null) {
			cachedBE.clear();
			return;
		}
		if (mc.isPaused() || mc.screen != null) return;
		var be = getBlockEntity();
		if (be instanceof EncasedFanBlockEntity
			|| be instanceof NozzleBlockEntity
			|| be instanceof ArmBlockEntity
			|| be instanceof EjectorBlockEntity
			|| be instanceof PackagePortBlockEntity) cachedBE.put(be, CCG.CONFIG.outliner.delayRenderDuration);
		if (cachedBE.isEmpty()) return;
		cachedBE.object2IntEntrySet().removeIf(entry -> {
			var blockEntity = entry.getKey();
			var newValue = entry.getIntValue() - 1;
			entry.setValue(newValue);
			if (!blockEntity.isRemoved()) switch (blockEntity) {
				case EncasedFanBlockEntity efbe -> render(efbe);
				case NozzleBlockEntity nbe -> render(nbe);
				case ArmBlockEntity abe -> render(abe);
				case EjectorBlockEntity ebe -> render(ebe);
				case PackagePortBlockEntity ppbe -> render(ppbe);
				default -> {}
			}
			return newValue <= 0;
		});
	}
	public static void render(@NotNull EncasedFanBlockEntity efbe) {
		var airCurrent = efbe.airCurrent;
		var color = getColor(airCurrent.pushing);
		var bounds = airCurrent.bounds;
		Outliner.getInstance()
			.chaseAABB("FanAirBox" + efbe, bounds)
			.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.lineWidth(1 / 16f)
			.colored(color);
		var numberOfFlowBoxes = getNumberOfFlowBoxes(airCurrent.maxDistance);
		for (var i = 0; i < numberOfFlowBoxes; i++) {
			var offset = getOffset(i, numberOfFlowBoxes);
			var flowBound = AirCurrentUtil.calculateFlowBound(airCurrent, bounds, offset);
			var id = "FanAirFlowBox" + efbe + i;
			if (offset > 0.98) {
				Outliner.getInstance().remove(id);
				continue;
			}
			Outliner.getInstance()
				.chaseAABB(id, flowBound)
				.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(color);
		}
	}
	public static void render(@NotNull NozzleBlockEntity nbe) {
		var accessor = (NozzleBlockEntityAccessor) nbe;
		var pushing = accessor.getPushing();
		var range = accessor.getRange();
		var center = nbe.getBlockPos().getCenter();
		var color = getColor(pushing);
		Outliner.getInstance()
			.chaseAABB("NozzleAirBox" + nbe, new AABB(center, center).inflate(range / 2f))
			.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.lineWidth(1 / 16f)
			.colored(color);
		var numberOfFlowBoxes = getNumberOfFlowBoxes(range);
		for (var i = 0; i < numberOfFlowBoxes; i++) {
			var offset = getOffset(i, numberOfFlowBoxes);
			var id = "NozzleAirFlowBox" + nbe + i;
			if (offset > 0.98) {
				Outliner.getInstance().remove(id);
				continue;
			}
			var radius = pushing ? offset * range / 2f : (1 - offset) * range / 2f;
			var flowBound = new AABB(center, center).inflate(radius);
			Outliner.getInstance()
				.chaseAABB(id, flowBound)
				.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(color);
		}
	}
	public static int getColor(boolean pushing) {
		return pushing ? CCG.CONFIG.outliner.outColor : CCG.CONFIG.outliner.inColor;
	}
	public static double getOffset(int i, int numberOfFlowBoxes) {
		return (System.currentTimeMillis() + i * ((double) 3000 / numberOfFlowBoxes)) % 3000 / 3000.0;
	}
	public static int getNumberOfFlowBoxes(float range) {
		return (int) (Math.log(range) + 1);
	}
	public static void render(@NotNull ArmBlockEntity abe) {
		var allPoints = new ArrayList<ArmInteractionPoint>();
		allPoints.addAll(abe.inputs);
		allPoints.addAll(abe.outputs);
		allPoints.forEach(point -> {
			if (!point.isValid()) return;
			var level = point.getLevel();
			var pos = point.getPos();
			Outliner.getInstance()
				.chaseAABB("ArmIOBox" + point, level.getBlockState(pos).getShape(level, pos).bounds().move(pos))
				.withFaceTextures(AllSpecialTextures.HIGHLIGHT_CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(point.getMode().getColor());
			Outliner.getInstance()
				.showLine("ArmIOLine" + point, abe.getBlockPos().getCenter(), point.getPos().getCenter())
				.lineWidth(1 / 8f)
				.colored(point.getMode().getColor());
		});
	}
	public static void render(@NotNull EjectorBlockEntity ebe) {
		Outliner.getInstance()
			.chaseAABB("EjectorTargetBox" + ebe, getBounds(ebe.getTargetPosition()))
			.lineWidth(1 / 16f)
			.colored(CCG.CONFIG.outliner.outColor);
	}
	public static void render(@NotNull PackagePortBlockEntity ppbe) {
		var mc = Minecraft.getInstance();
		var pos = ppbe.getBlockPos();
		if (ppbe.target == null) return;
		var source = Vec3.atBottomCenterOf(pos);
		var target = ppbe.target.getExactTargetLocation(ppbe, mc.level, pos);
		if (target == Vec3.ZERO) return;
		var color = 0x9ede73;
		Outliner.getInstance().showLine("PackagePortConnection" + ppbe, source, target).lineWidth(1 / 8f).colored(color);
		Outliner.getInstance()
			.chaseAABB("ChainPointSelected" + ppbe, new AABB(target, target))
			.colored(color)
			.lineWidth(1 / 5f)
			.disableLineNormals();
	}
}
