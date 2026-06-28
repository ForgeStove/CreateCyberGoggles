package io.github.forgestove.create_cyber_goggles.core.overlay;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup.PointForce;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import org.jetbrains.annotations.Nullable;

import java.util.*;
/**
 * Holds the latest force-diagram snapshot received from the server.
 * <p>
 * The {@link DiagramDataPacket} does <em>not</em> carry the sublevel UUID, so
 * we store a single "latest" snapshot. Upstream code ({@link ForceOverlayClient})
 * pairs it with whichever target it has requested from.
 */
public final class ForceDataCache {
	private static volatile Map<ForceGroup, List<PointForce>> latestForces;
	private static volatile double latestMass;
	private static volatile boolean dirty;
	private ForceDataCache() {
	}
	/** Called by the mixin when a new packet arrives. */
	public static void set(Map<ForceGroup, List<PointForce>> forces, double mass) {
		latestForces = forces;
		latestMass = mass;
		dirty = true;
	}
	/** Atomically read and consume the "dirty" flag. */
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
	/** Reset all state (e.g. when the player stops targeting). */
	public static void clear() {
		latestForces = null;
		latestMass = 0;
		dirty = false;
	}
}
