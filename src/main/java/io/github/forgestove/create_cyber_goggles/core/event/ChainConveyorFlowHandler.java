package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.kinetics.chainConveyor.*;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectionStats;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;

import static io.github.forgestove.create_cyber_goggles.core.event.Outliner.getColor;
import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public final class ChainConveyorFlowHandler {
	/** 累计真实时间(tick)，替代离散 gameTime 作为流动基准，与 TPS 解耦 */
	private static double flowTime;
	public static void render(RenderLevelStageEvent event) {
		if (!CCG.config.outliner.renderAnalogBox) return;
		if (event.getStage() != Stage.AFTER_LEVEL) return;
		if (shouldSuppressInfo() || mc.isPaused() || isInGUI() || mc.level == null || mc.player == null) return;
		BlockPos liftPos = ChainConveyorInteractionHandler.selectedLift;
		BlockPos connection = ChainConveyorInteractionHandler.selectedConnection;
		if (liftPos == null || connection == null) return;
		if (mc.level.getBlockEntity(liftPos) instanceof ChainConveyorBlockEntity ccbe) renderFlow(ccbe, connection);
	}
	private static void renderFlow(ChainConveyorBlockEntity ccbe, BlockPos connection) {
		ConnectionStats stats = ccbe.connectionStats.get(connection);
		if (stats == null) return;
		float speed = ccbe.getSpeed();
		if (speed == 0) return;
		Vec3 start = stats.start();
		Vec3 end = stats.end();
		Vec3 dir = end.subtract(start).normalize();
		float length = stats.chainLength();
		// 每段线段长度与间隔
		var segLen = 0.25;
		var gap = 0.5;
		double period = segLen + gap;
		var count = (int) Math.floor(length / period);
		if (count <= 0) return;
		flowTime += getRealtimeDeltaTicks();
		double travel = Math.abs(speed) / 360.0;
		double cycle = period / Math.clamp(travel, 0.05, 0.1);
		double phase = flowTime / cycle % 1.0 * period;
		String id = "ChainConveyorFlow" + ccbe.getBlockPos() + connection;
		int color = getColor(!ccbe.reversed);
		for (var i = 0; i < count; i++) {
			double offset = i * period + phase;
			if (offset + segLen > length) continue;
			Vec3 segStart = start.add(dir.scale(offset));
			outliner.showLine(id + i, segStart, segStart.add(dir.scale(segLen))).lineWidth(1 / 8f).colored(color);
		}
	}
}
