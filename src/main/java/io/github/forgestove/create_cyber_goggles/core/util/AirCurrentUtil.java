package io.github.forgestove.create_cyber_goggles.core.util;
import com.zurrtum.create.content.kinetics.fan.AirCurrent;
import net.minecraft.world.phys.AABB;
public class AirCurrentUtil {
	public static AABB calculateFlowBound(AirCurrent airCurrent, AABB bounds, double offset) {
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
		var pos = airCurrent.pushing == airCurrent.direction.getAxisDirection().getStep() > 0 ? min + offsetDistance :
			max - offsetDistance;
		return switch (axis) {
			case X -> new AABB(pos, bounds.minY, bounds.minZ, pos, bounds.maxY, bounds.maxZ);
			case Y -> new AABB(bounds.minX, pos, bounds.minZ, bounds.maxX, pos, bounds.maxZ);
			case Z -> new AABB(bounds.minX, bounds.minY, pos, bounds.maxX, bounds.maxY, pos);
		};
	}
}
