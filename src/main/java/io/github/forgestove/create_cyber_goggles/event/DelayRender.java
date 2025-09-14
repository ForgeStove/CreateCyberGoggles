package io.github.forgestove.create_cyber_goggles.event;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.content.kinetics.fan.*;
import com.simibubi.create.content.kinetics.mechanicalArm.*;
import io.github.forgestove.create_cyber_goggles.*;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class DelayRender {
	public static Object2IntOpenHashMap<BlockEntity> cachedBE = new Object2IntOpenHashMap<>();
	public static void tick() {
		if (!CCG.CONFIG.delayRender.renderAnalogBox) return;
		var mc = Minecraft.getInstance();
		if (mc.level == null) {
			cachedBE.clear();
			return;
		}
		if (mc.isPaused() || mc.screen != null) return;
		var be = Common.getSelectedBE();
		if (be instanceof EncasedFanBlockEntity || be instanceof NozzleBlockEntity || be instanceof ArmBlockEntity)
			cachedBE.put(be, CCG.CONFIG.delayRender.delayRenderDuration);
		if (cachedBE.isEmpty()) return;
		cachedBE.object2IntEntrySet().removeIf(entry -> {
			var blockEntity = entry.getKey();
			var newValue = entry.getIntValue() - 1;
			entry.setValue(newValue);
			if (!blockEntity.isRemoved()) switch (blockEntity) {
				case EncasedFanBlockEntity efbe -> render(efbe);
				case NozzleBlockEntity nbe -> render(nbe);
				case ArmBlockEntity abe -> render(abe);
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
			.chaseAABB("FanAirBox" + efbe.getBlockPos(), bounds)
			.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.lineWidth(1 / 16f)
			.colored(color);
		var numberOfFlowBoxes = getNumberOfFlowBoxes(airCurrent.maxDistance);
		for (var i = 0; i < numberOfFlowBoxes; i++) {
			var offset = getOffset(i, numberOfFlowBoxes);
			var offsetDistance = airCurrent.maxDistance * offset;
			var axis = airCurrent.direction.getAxis();
			var min = switch (axis) {
				case X -> bounds.minX;
				case Y -> bounds.minY;
				case Z -> bounds.minZ;
			};
			var max = switch (axis) {
				case X -> bounds.maxX;
				case Y -> bounds.maxY;
				case Z -> bounds.maxZ;
			};
			var pos = airCurrent.pushing == airCurrent.direction.getAxisDirection().getStep() > 0
				? min + offsetDistance
				: max - offsetDistance;
			var flowBound = switch (axis) {
				case X -> new AABB(pos, bounds.minY, bounds.minZ, pos, bounds.maxY, bounds.maxZ);
				case Y -> new AABB(bounds.minX, pos, bounds.minZ, bounds.maxX, pos, bounds.maxZ);
				case Z -> new AABB(bounds.minX, bounds.minY, pos, bounds.maxX, bounds.maxY, pos);
			};
			var id = "FanAirFlowBox" + efbe.getBlockPos() + i;
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
		var center = VecHelper.getCenterOf(nbe.getBlockPos());
		var color = getColor(nbe.pushing);
		Outliner.getInstance()
			.chaseAABB("NozzleAirBox" + nbe.getBlockPos(), new AABB(center, center).inflate(nbe.range / 2f))
			.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.lineWidth(1 / 16f)
			.colored(color);
		var numberOfFlowBoxes = getNumberOfFlowBoxes(nbe.range);
		for (var i = 0; i < numberOfFlowBoxes; i++) {
			var offset = getOffset(i, numberOfFlowBoxes);
			var id = "NozzleAirFlowBox" + nbe.getBlockPos() + i;
			if (offset > 0.98) {
				Outliner.getInstance().remove(id);
				continue;
			}
			var radius = nbe.pushing ? offset * nbe.range / 2f : (1 - offset) * nbe.range / 2f;
			var flowBound = new AABB(center, center).inflate(radius);
			Outliner.getInstance()
				.chaseAABB(id, flowBound)
				.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(color);
		}
	}
	public static int getColor(boolean pushing) {
		return pushing ? CCG.CONFIG.delayRender.airBoxPushColor : CCG.CONFIG.delayRender.airBoxPullColor;
	}
	public static double getOffset(int i, int numberOfFlowBoxes) {
		return (System.currentTimeMillis() + i * ((double) 3000 / numberOfFlowBoxes)) % 3000 / 3000.0;
	}
	public static int getNumberOfFlowBoxes(float range) {
		return (int) (Math.log(range) + 1);
	}
	public static void render(@NotNull ArmBlockEntity abe) {
		drawArmIO(abe, abe.inputs);
		drawArmIO(abe, abe.outputs);
	}
	public static void drawArmIO(@NotNull ArmBlockEntity abe, List<ArmInteractionPoint> list) {
		list.forEach(point -> {
			if (!point.isValid()) {
				list.remove(point);
				return;
			}
			var level = point.getLevel();
			var pos = point.getPos();
			Outliner.getInstance()
				.chaseAABB("ArmIOBox" + point, level.getBlockState(pos).getShape(level, pos).bounds().move(pos))
				.withFaceTextures(AllSpecialTextures.HIGHLIGHT_CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(point.getMode().getColor());
			Outliner.getInstance()
				.showLine("ArmIOLine" + point, abe.getBlockPos().getCenter(), point.getPos().getCenter())
				.lineWidth(1 / 16f)
				.colored(point.getMode().getColor());
		});
	}
}
