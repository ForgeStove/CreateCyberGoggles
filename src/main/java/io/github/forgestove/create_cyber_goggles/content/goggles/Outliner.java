package io.github.forgestove.create_cyber_goggles.content.goggles;
import com.mojang.logging.LogUtils;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.OutlineRenderable;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import org.jetbrains.annotations.*;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Map.Entry;

import static io.github.forgestove.create_cyber_goggles.foundation.util.CCGUtil.*;
public class Outliner {
	public static final Map<BlockEntity, Integer> cachedBE = new Object2IntOpenHashMap<>();
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final long FLOW_PERIOD = 3000;
	private static long tickMillis = 50;
	private static long lastTickMillis;
	public static void tick(Post ignoredEvent) {
		long now = System.currentTimeMillis();
		long delta = now - lastTickMillis;
		lastTickMillis = now;
		if (delta > 0 && delta < 1000) tickMillis = delta;
		if (!CCG.config.outliner.renderAnalogBox) return;
		if (shouldSuppressInfo()) return;
		if (mc.isPaused() || isInGUI()) return;
		BlockEntity be = getBlockEntity();
		if (be instanceof OutlineRenderable or) cachedBE.put(be, or.ccg$getRenderDelay());
		if (cachedBE.isEmpty()) return;
		try {
			cachedBE.entrySet().removeIf(Outliner::render);
		} catch (Throwable throwable) {
			LOGGER.error("Failed to tick outlines", throwable);
		}
	}
	private static boolean render(@NotNull Entry<BlockEntity, Integer> entry) {
		int nextDelay = entry.getValue() - 1;
		entry.setValue(nextDelay);
		BlockEntity be = entry.getKey();
		if (!be.isRemoved() && be instanceof OutlineRenderable or) or.ccg$render();
		return nextDelay <= 0;
	}
	@Contract(pure = true)
	public static int getColor(boolean pushing) {
		return pushing ? CCG.config.outliner.outColor : CCG.config.outliner.inColor;
	}
	public static double getOffsetScale(long millis, int i, int numberOfFlowBoxes) {
		double phase = getFlowPhase(millis, i, numberOfFlowBoxes);
		double passEnd = ((long) (phase / FLOW_PERIOD) + 1) * FLOW_PERIOD;
		return phase + tickMillis >= passEnd ? 1 : phase % FLOW_PERIOD / FLOW_PERIOD;
	}
	public static long getCycleIndex(long millis, int i, int numberOfFlowBoxes) {
		return (long) (getFlowPhase(millis, i, numberOfFlowBoxes) / FLOW_PERIOD);
	}
	private static double getFlowPhase(long millis, int i, int numberOfFlowBoxes) {
		return millis + i * (FLOW_PERIOD / (double) numberOfFlowBoxes);
	}
	@Contract(pure = true)
	public static int getNumberOfFlowBoxes(float range) {
		return (int) (Math.log(range) + 1);
	}
}
