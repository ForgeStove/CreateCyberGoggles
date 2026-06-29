package io.github.forgestove.create_cyber_goggles.core.event.forceOverlay;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup.PointForce;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import org.jetbrains.annotations.Nullable;

import java.util.*;
/**
 * 保存从服务器接收的最新力图快照。
 * <p>
 * {@link DiagramDataPacket} 不携带子层级 UUID，因此我们只存储单个"最新"快照。
 * 上游代码（{@link ForceOverlay}）将其与所请求的目标配对。
 */
public final class ForceDataCache {
	private static volatile Map<ForceGroup, List<PointForce>> latestForces;
	private static volatile double latestMass;
	private static volatile boolean dirty;
	private ForceDataCache() {
	}
	/** 当新的数据包到达时由 mixin 调用。 */
	public static void set(Map<ForceGroup, List<PointForce>> forces, double mass) {
		latestForces = forces;
		latestMass = mass;
		dirty = true;
	}
	/** 原子性地读取并消费"dirty"标记。 */
	public static boolean consumeDirty() {
		var d = dirty;
		dirty = false;
		return d;
	}
	@Nullable
	public static Map<ForceGroup, List<PointForce>> getLatestForces() {
		return latestForces;
	}
	public static double getLatestMass() {
		return latestMass;
	}
	/** 重置所有状态（例如当玩家停止瞄准时）。 */
	public static void clear() {
		latestForces = null;
		latestMass = 0;
		dirty = false;
	}
}
