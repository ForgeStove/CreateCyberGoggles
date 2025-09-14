package io.github.forgestove.create_cyber_goggles.event;
import com.simibubi.create.content.kinetics.fan.*;
import io.github.forgestove.create_cyber_goggles.*;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
public class AirBoxRender {
	public static Object2IntOpenHashMap<BlockEntity> cachedBE = new Object2IntOpenHashMap<>();
	public static void tick() {
		if (!CCG.CONFIG.renderBox.renderAirBox) return;
		var mc = Minecraft.getInstance();
		if (mc.level == null) {
			cachedBE.clear();
			return;
		}
		if (mc.isPaused() || mc.screen != null) return;
		var be = Common.getSelectedBE();
		if (be instanceof EncasedFanBlockEntity || be instanceof NozzleBlockEntity) cachedBE.put(be, 120);
		if (cachedBE.isEmpty()) return;
		cachedBE.object2IntEntrySet().removeIf(entry -> {
			var blockEntity = entry.getKey();
			var newValue = entry.getIntValue() - 1;
			entry.setValue(newValue);
			if (!blockEntity.isRemoved()) switch (blockEntity) {
				case EncasedFanBlockEntity efbe -> render(efbe);
				case NozzleBlockEntity nbe -> render(nbe);
				default -> {}
			}
			return newValue <= 0;
		});
	}
	public static int getColor(boolean pushing) {
		return pushing ? CCG.CONFIG.renderBox.airBoxPushColor : CCG.CONFIG.renderBox.airBoxPullColor;
	}
	public static double getOffset(int i, int numberOfFlowBoxes) {
		return (System.currentTimeMillis() + i * ((double) 3000 / numberOfFlowBoxes)) % 3000 / 3000.0;
	}
	public static int getNumberOfFlowBoxes(float range) {
		return Math.max(1, (int) (range / 3));
	}
	public static void render(@NotNull EncasedFanBlockEntity efbe) {
		var airCurrent = efbe.airCurrent;
		var color = getColor(airCurrent.pushing);
		var bounds = airCurrent.bounds;
		Outliner.getInstance().chaseAABB("FanAirBox" + efbe.getBlockPos(), bounds).colored(color);
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
			Outliner.getInstance().chaseAABB(id, flowBound).colored(color);
		}
	}
	public static void render(@NotNull NozzleBlockEntity nbe) {
		var center = VecHelper.getCenterOf(nbe.getBlockPos());
		var color = getColor(nbe.pushing);
		Outliner.getInstance()
			.chaseAABB("NozzleAirBox" + nbe.getBlockPos(), new AABB(center, center).inflate(nbe.range / 2f))
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
			Outliner.getInstance().chaseAABB(id, flowBound).colored(color);
		}
	}
}
