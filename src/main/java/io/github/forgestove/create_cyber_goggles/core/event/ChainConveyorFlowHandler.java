package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.kinetics.chainConveyor.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.core.BlockPos;
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
		var liftPos = ChainConveyorInteractionHandler.selectedLift;
		var connection = ChainConveyorInteractionHandler.selectedConnection;
		if (liftPos == null || connection == null) return;
		if (mc.level.getBlockEntity(liftPos) instanceof ChainConveyorBlockEntity ccbe) renderFlow(ccbe, connection);
	}
	private static void renderFlow(ChainConveyorBlockEntity ccbe, BlockPos connection) {
		var stats = ccbe.connectionStats.get(connection);
		if (stats == null) return;
		var speed = ccbe.getSpeed();
		if (speed == 0) return;
		var start = stats.start();
		var end = stats.end();
		var dir = end.subtract(start).normalize();
		var length = stats.chainLength();
		// 每段线段长度与间隔
		var segLen = 0.25;
		var gap = 0.5;
		var period = segLen + gap;
		var count = (int) Math.floor(length / period);
		if (count <= 0) return;
		flowTime += getRealtimeDeltaTicks();
		var travel = Math.abs(speed) / 360.0;
		var cycle = period / Math.clamp(travel, 0.05, 0.1);
		var phase = flowTime / cycle % 1.0 * period;
		var id = "ChainConveyorFlow" + ccbe.getBlockPos() + connection;
		var color = getColor(!ccbe.reversed);
		for (var i = 0; i < count; i++) {
			var offset = i * period + phase;
			if (offset + segLen > length) continue;
			var segStart = start.add(dir.scale(offset));
			outliner.showLine(id + i, segStart, segStart.add(dir.scale(segLen))).lineWidth(1 / 8f).colored(color);
		}
	}
}
