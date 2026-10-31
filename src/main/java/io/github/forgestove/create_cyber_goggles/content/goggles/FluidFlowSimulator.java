package io.github.forgestove.create_cyber_goggles.content.goggles;
import com.simibubi.create.content.fluids.*;
import com.simibubi.create.content.fluids.PipeConnection.Flow;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ICapabilityProvider;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.Nullable;

import java.util.*;
public final class FluidFlowSimulator {
	private static final Map<FluidTransportBehaviour, Map<Direction, Integer>> CACHE = new IdentityHashMap<>();
	private static final Map<FluidTankBlockEntity, int[]> TANK_CACHE = new IdentityHashMap<>();
	private static long cacheTick = Long.MIN_VALUE;
	private static long tankCacheTick = Long.MIN_VALUE;
	public static Map<Direction, Integer> rates(FluidTransportBehaviour behaviour) {
		Level level = behaviour.getWorld();
		long tick = level.getGameTime();
		if (tick != cacheTick) {
			CACHE.clear();
			cacheTick = tick;
		}
		return CACHE.computeIfAbsent(behaviour, ignored -> simulate(level, behaviour));
	}
	private static Map<Direction, Integer> simulate(Level level, FluidTransportBehaviour start) {
		Map<BlockFace, BlockFace> inboundSources = new HashMap<>();
		for (Direction direction : Direction.values()) {
			Flow flow = start.getFlow(direction);
			if (flow == null || !flow.inbound) continue;
			var face = new BlockFace(start.getPos(), direction);
			BlockFace source = findSource(level, face);
			if (source != null) inboundSources.put(face, source);
		}
		Map<Direction, Integer> result = new EnumMap<>(Direction.class);
		boolean resolved = false;
		for (BlockFace source : new HashSet<>(inboundSources.values())) {
			Map<Direction, Integer> partial = simulateNetwork(level, start, source, inboundSources);
			if (partial == null) continue;
			resolved = true;
			partial.forEach((direction, value) -> result.merge(direction, value, Integer::sum));
		}
		return resolved ? result : new EnumMap<>(Direction.class);
	}
	@Nullable
	private static Map<Direction, Integer> simulateNetwork(
		Level level,
		FluidTransportBehaviour start,
		BlockFace source,
		Map<BlockFace, BlockFace> inboundSources
	) {
		Set<BlockPos> pipes = new HashSet<>();
		List<BlockFace> targets = collectTargets(level, source, pipes);
		if (!pipes.contains(start.getPos())) return null;
		if (targets.isEmpty()) return zeroes(start);
		Map<BlockFace, Integer> allotted = allocate(level, source, targets, true);
		if (allotted == null) return null;
		boolean onPath = pathPipes(level, targets).contains(start.getPos());
		Map<Direction, Integer> outbound = new EnumMap<>(Direction.class);
		var downstreamTotal = 0;
		for (Direction direction : Direction.values()) {
			PipeConnection pipeConnection = start.getConnection(direction);
			if (pipeConnection == null || !pipeConnection.hasFlow()) continue;
			Flow flow = start.getFlow(direction);
			if (flow == null || flow.inbound) continue;
			var face = new BlockFace(start.getPos(), direction);
			Integer value = allotted.get(face);
			if (value == null) value = downstream(level, face, allotted, new HashSet<>());
			outbound.put(direction, value);
			downstreamTotal += value;
		}
		Map<Direction, Integer> result = new EnumMap<>(Direction.class);
		for (Direction direction : Direction.values()) {
			PipeConnection pipeConnection = start.getConnection(direction);
			if (pipeConnection == null || !pipeConnection.hasFlow()) continue;
			Integer value = outbound.get(direction);
			if (value == null) {
				var face = new BlockFace(start.getPos(), direction);
				value = onPath && source.equals(inboundSources.get(face)) ? downstreamTotal : 0;
			}
			result.put(direction, value);
		}
		return result;
	}
	private static int downstream(Level level, BlockFace face, Map<BlockFace, Integer> allotted, Set<BlockPos> visited) {
		Integer direct = allotted.get(face);
		if (direct != null) return direct;
		BlockPos pos = face.getConnectedPos();
		FluidTransportBehaviour pipe = behaviour(level, pos);
		if (pipe == null || !visited.add(pos)) return 0;
		var sum = 0;
		for (Direction direction : Direction.values()) {
			Flow flow = pipe.getFlow(direction);
			if (flow == null || flow.inbound || !flow.complete) continue;
			sum += downstream(level, new BlockFace(pos, direction), allotted, visited);
		}
		return sum;
	}
	private static Map<Direction, Integer> zeroes(FluidTransportBehaviour start) {
		Map<Direction, Integer> result = new EnumMap<>(Direction.class);
		for (Direction direction : Direction.values())
			if (start.getFlow(direction) != null) result.put(direction, 0);
		return result;
	}
	@Nullable
	private static Map<BlockFace, Integer> allocate(Level level, BlockFace source, List<BlockFace> targets, boolean compensate) {
		Map<BlockFace, IFluidHandler> handlers = new HashMap<>();
		for (BlockFace face : targets) {
			IFluidHandler handler = handler(level, face);
			if (handler != null) handlers.put(face, handler);
		}
		if (handlers.isEmpty()) return null;
		FluidTransportBehaviour sourceBehaviour = behaviour(level, source.getPos());
		if (sourceBehaviour == null) return null;
		PipeConnection connection = sourceBehaviour.getConnection(source.getFace());
		Flow flow = sourceBehaviour.getFlow(source.getFace());
		if (connection == null || flow == null || !flow.inbound || !flow.complete) return null;
		IFluidHandler sourceHandler = handler(level, source);
		if (sourceHandler == null) return null;
		Map<BlockFace, Integer> released = new HashMap<>();
		if (compensate) for (BlockFace face : handlers.keySet()) released.put(face, released(level, face, source));
		var speed = (int) Math.max(1, connection.getPressure().get(true) / 2f);
		FluidStack transfer = drain(sourceHandler, speed, flow.fluid);
		Map<BlockFace, Integer> allotted = new HashMap<>();
		if (transfer.isEmpty()) return allotted;
		Map<IFluidHandler, Integer> accepted = new IdentityHashMap<>();
		List<BlockFace> available = new ArrayList<>();
		for (BlockFace face : targets)
			if (handlers.containsKey(face)) available.add(face);
		while (!available.isEmpty() && !transfer.isEmpty()) {
			int divided = transfer.getAmount() / available.size();
			int remainder = transfer.getAmount() % available.size();
			for (Iterator<BlockFace> iterator = available.iterator(); iterator.hasNext(); ) {
				BlockFace face = iterator.next();
				if (transfer.isEmpty()) break;
				int share = divided + (remainder-- > 0 ? 1 : 0);
				IFluidHandler target = handlers.get(face);
				int request = share + accepted.getOrDefault(target, 0);
				FluidStack stack = transfer.copy();
				stack.setAmount(request);
				int fill = Math.clamp(target.fill(stack, FluidAction.SIMULATE), 0, request);
				accepted.put(target, fill);
				int gained = fill - (request - share) + released.getOrDefault(face, 0);
				gained = Math.clamp(gained, 0, share);
				if (gained > 0) allotted.merge(face, gained, Integer::sum);
				transfer.setAmount(Math.max(0, transfer.getAmount() - gained));
				if (fill < request) iterator.remove();
			}
		}
		return allotted;
	}
	public static int[] tankThroughput(Level level, FluidTankBlockEntity controller) {
		long tick = level.getGameTime();
		if (tick != tankCacheTick) {
			TANK_CACHE.clear();
			tankCacheTick = tick;
		}
		return TANK_CACHE.computeIfAbsent(controller, ignored -> calculateTankThroughput(level, controller));
	}
	private static int[] calculateTankThroughput(Level level, FluidTankBlockEntity controller) {
		var input = 0;
		var output = 0;
		BlockPos origin = controller.getBlockPos();
		int width = controller.getWidth();
		int height = controller.getHeight();
		for (var x = 0; x < width; x++)
			for (var y = 0; y < height; y++)
				for (var z = 0; z < width; z++) {
					BlockPos pos = origin.offset(x, y, z);
					for (Direction direction : Direction.values()) {
						FluidTransportBehaviour pipe = behaviour(level, pos.relative(direction));
						if (pipe == null) continue;
						Direction towardTank = direction.getOpposite();
						Flow flow = pipe.getFlow(towardTank);
						if (flow == null || !flow.complete) continue;
						Integer rate = rates(pipe).get(towardTank);
						if (rate == null) continue;
						if (flow.inbound) output += rate;
						else input += rate;
					}
				}
		return new int[]{input, output};
	}
	private static int released(Level level, BlockFace target, BlockFace source) {
		BlockPos container = target.getConnectedPos();
		if (!FluidPropagator.hasFluidCapability(level, container, target.getOppositeFace())) return 0;
		var total = 0;
		for (Direction direction : Direction.values()) {
			BlockPos pipePos = container.relative(direction);
			FluidTransportBehaviour pipe = behaviour(level, pipePos);
			if (pipe == null) continue;
			Direction towardContainer = direction.getOpposite();
			var face = new BlockFace(pipePos, towardContainer);
			if (face.getPos().equals(source.getPos()) && face.getFace() == source.getFace()) continue;
			Flow flow = pipe.getFlow(towardContainer);
			if (flow == null || !flow.inbound || !flow.complete) continue;
			if (!isEndpoint(level, face)) continue;
			Set<BlockPos> pipes = new HashSet<>();
			List<BlockFace> targets = collectTargets(level, face, pipes);
			if (targets.isEmpty()) continue;
			Map<BlockFace, Integer> allocated = allocate(level, face, targets, false);
			if (allocated != null) total += allocated.values().stream().mapToInt(Integer::intValue).sum();
		}
		return total;
	}
	@Nullable
	private static BlockFace findSource(Level level, BlockFace face) {
		Deque<BlockFace> queue = new ArrayDeque<>();
		Set<BlockPos> visited = new HashSet<>();
		queue.add(face);
		while (!queue.isEmpty()) {
			BlockFace current = queue.poll();
			if (isEndpoint(level, current)) return current;
			if (!visited.add(current.getConnectedPos())) continue;
			FluidTransportBehaviour next = behaviour(level, current.getConnectedPos());
			if (next == null) continue;
			for (Direction direction : Direction.values()) {
				Flow flow = next.getFlow(direction);
				if (flow == null || !flow.inbound) continue;
				queue.add(new BlockFace(next.getPos(), direction));
			}
		}
		return null;
	}
	private static List<BlockFace> collectTargets(Level level, BlockFace source, Set<BlockPos> pipes) {
		List<BlockFace> targets = new ArrayList<>();
		Deque<BlockFace> queue = new ArrayDeque<>();
		queue.add(source);
		while (!queue.isEmpty()) {
			BlockFace current = queue.poll();
			BlockPos pos = current.getPos();
			Direction from = current.getFace();
			if (!pipes.add(pos)) continue;
			FluidTransportBehaviour behaviour = behaviour(level, pos);
			if (behaviour == null) continue;
			for (Direction direction : Direction.values()) {
				if (direction == from) continue;
				Flow flow = behaviour.getFlow(direction);
				if (flow == null || flow.inbound || !flow.complete) continue;
				var face = new BlockFace(pos, direction);
				if (isEndpoint(level, face)) targets.add(face);
				else queue.add(new BlockFace(face.getConnectedPos(), direction.getOpposite()));
			}
		}
		return targets;
	}
	private static boolean isEndpoint(Level level, BlockFace face) {
		return FluidPropagator.isOpenEnd(level, face.getPos(), face.getFace()) || FluidPropagator.hasFluidCapability(
			level,
			face.getConnectedPos(),
			face.getOppositeFace()
		);
	}
	@Nullable
	private static IFluidHandler handler(Level level, BlockFace face) {
		if (FluidPropagator.isOpenEnd(level, face.getPos(), face.getFace())) {
			var pipe = new OpenEndedPipe(face);
			pipe.manageSource(level, null);
			ICapabilityProvider<IFluidHandler> provider = pipe.provideHandler();
			return provider == null ? null : provider.getCapability();
		}
		BlockPos connected = face.getConnectedPos();
		Direction side = face.getOppositeFace();
		if (!FluidPropagator.hasFluidCapability(level, connected, side)) return null;
		return level.getCapability(FluidHandler.BLOCK, connected, side);
	}
	private static FluidStack drain(IFluidHandler handler, int speed, FluidStack fluid) {
		for (var i = 0; i < handler.getTanks(); i++) {
			FluidStack contained = handler.getFluidInTank(i);
			if (contained.isEmpty() || !FluidStack.isSameFluidSameComponents(contained, fluid)) continue;
			return handler.drain(FluidHelper.copyStackWithAmount(contained, speed), FluidAction.SIMULATE);
		}
		FluidStack drained = handler.drain(speed, FluidAction.SIMULATE);
		return FluidStack.isSameFluidSameComponents(drained, fluid) ? drained : FluidStack.EMPTY;
	}
	private static Set<BlockPos> pathPipes(Level level, List<BlockFace> targets) {
		Set<BlockPos> path = new HashSet<>();
		Deque<BlockPos> queue = new ArrayDeque<>();
		for (BlockFace target : targets) queue.add(target.getPos());
		while (!queue.isEmpty()) {
			BlockPos pos = queue.poll();
			FluidTransportBehaviour pipe = behaviour(level, pos);
			if (pipe == null || !path.add(pos)) continue;
			for (Direction direction : Direction.values()) {
				Flow flow = pipe.getFlow(direction);
				if (flow != null && flow.inbound) queue.add(pos.relative(direction));
			}
		}
		return path;
	}
	@Nullable
	private static FluidTransportBehaviour behaviour(Level level, BlockPos pos) {
		return BlockEntityBehaviour.get(level, pos, FluidTransportBehaviour.TYPE);
	}
}
