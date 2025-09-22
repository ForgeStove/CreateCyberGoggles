package io.github.forgestove.create_cyber_goggles.event;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.content.kinetics.fan.*;
import com.simibubi.create.content.kinetics.mechanicalArm.*;
import com.simibubi.create.content.logistics.depot.EjectorBlockEntity;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import io.github.forgestove.create_cyber_goggles.*;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.*;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
public class DelayRender {
	public static Object2IntOpenHashMap<BlockEntity> cachedBE = new Object2IntOpenHashMap<>();
	public static void tick(ClientTickEvent ignoredEvent) {
		if (!CCG.CONFIG.delayRender.renderAnalogBox) return;
		var mc = Minecraft.getInstance();
		if (mc.level == null) {
			cachedBE.clear();
			return;
		}
		if (mc.isPaused() || mc.screen != null) return;
		var be = Common.getBE();
		if (be instanceof EncasedFanBlockEntity
			|| be instanceof NozzleBlockEntity
			|| be instanceof ArmBlockEntity
			|| be instanceof EjectorBlockEntity
			|| be instanceof PackagePortBlockEntity) cachedBE.put(be, CCG.CONFIG.delayRender.delayRenderDuration);
		if (cachedBE.isEmpty()) return;
		cachedBE.object2IntEntrySet().removeIf(entry -> {
			var blockEntity = entry.getKey();
			var newValue = entry.getIntValue() - 1;
			entry.setValue(newValue);
			if (!blockEntity.isRemoved()) if (blockEntity instanceof EncasedFanBlockEntity efbe) render(efbe);
			else if (blockEntity instanceof NozzleBlockEntity nbe) render(nbe);
			else if (blockEntity instanceof ArmBlockEntity abe) render(abe);
			else if (blockEntity instanceof EjectorBlockEntity ebe) render(ebe);
			else if (blockEntity instanceof PackagePortBlockEntity ppbe) render(ppbe);
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
		return pushing ? CCG.CONFIG.delayRender.windPushColor : CCG.CONFIG.delayRender.windPullColor;
	}
	public static double getOffset(int i, int numberOfFlowBoxes) {
		return (System.currentTimeMillis() + i * ((double) 3000 / numberOfFlowBoxes)) % 3000 / 3000.0;
	}
	public static int getNumberOfFlowBoxes(float range) {
		return (int) (Math.log(range) + 1);
	}
	public static void render(@NotNull ArmBlockEntity abe) {
		var accessor = (ArmBlockEntityAccessor) abe;
		var allPoints = new ArrayList<ArmInteractionPoint>();
		allPoints.addAll(accessor.getInputs());
		allPoints.addAll(accessor.getOutputs());
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
			.chaseAABB("EjectorTargetBox" + ebe, new AABB(ebe.getTargetPosition()))
			.lineWidth(1 / 16f)
			.colored(CCG.CONFIG.delayRender.windPushColor);
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
