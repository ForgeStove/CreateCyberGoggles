package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.kinetics.chainConveyor.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;

import static io.github.forgestove.create_cyber_goggles.core.event.Outliner.getColor;
import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public final class ChainConveyorFlowHandler {
	public static void render(RenderLevelStageEvent event) {
		if (!CCG.config.outliner.renderAnalogBox) return;
		if (event.getStage() != Stage.AFTER_LEVEL) return;
		if (shouldSuppressInfo() || mc.isPaused() || isInGUI() || mc.level == null || mc.player == null) return;
		var liftPos = ChainConveyorInteractionHandler.selectedLift;
		var connection = ChainConveyorInteractionHandler.selectedConnection;
		if (liftPos == null || connection == null) return;
		if (mc.level.getBlockEntity(liftPos) instanceof ChainConveyorBlockEntity ccbe)
			renderFlow(ccbe, connection, event.getPartialTick().getGameTimeDeltaPartialTick(true));
	}
	private static void renderFlow(ChainConveyorBlockEntity ccbe, BlockPos connection, float partialTick) {
		var stats = ccbe.connectionStats.get(connection);
		if (stats == null) return;
		var speed = ccbe.getSpeed();
		if (speed == 0) return;
		var start = stats.start();
		var end = stats.end();
		var dir = end.subtract(start).normalize();
		var length = stats.chainLength();
		// 每段线段长度与间隔
		var segLen = 0.35;
		var gap = 0.55;
		var period = segLen + gap;
		var count = (int) Math.floor(length / period);
		if (count <= 0) return;
		// 流动：连续时间（gameTime + partialTick）按线速度推进，速度符号决定方向
		var travel = Math.abs(speed) / 360.0;
		var cycle = period / Math.clamp(travel, 0.05, 0.2);
		if (mc.level == null) return;
		var time = mc.level.getGameTime() + partialTick;
		var phase = time / cycle % 1.0 * period;
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
