package io.github.forgestove.create_cyber_goggles.core.event;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.OutlineRenderable;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import org.jetbrains.annotations.*;

import java.util.Map;
import java.util.Map.Entry;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class Outliner {
	public static final Map<BlockEntity, Integer> cachedBE = new Object2IntOpenHashMap<>();
	public static void tick(Post ignoredEvent) {
		if (!CCG.config.outliner.renderAnalogBox) return;
		if (mc.isPaused() || isInGUI()) return;
		var be = getBlockEntity();
		if (be instanceof OutlineRenderable) cachedBE.put(be, CCG.config.outliner.delayRenderDuration);
		if (cachedBE.isEmpty()) return;
		try {
			cachedBE.entrySet().removeIf(Outliner::render);
		} catch (Throwable throwable) {
			CCG.LOGGER.error(throwable.getMessage(), throwable);
		}
	}
	private static boolean render(@NotNull Entry<BlockEntity, Integer> entry) {
		var nextDelay = entry.getValue() - 1;
		entry.setValue(nextDelay);
		var be = entry.getKey();
		if (!be.isRemoved() && be instanceof OutlineRenderable or) or.ccg$render();
		return nextDelay <= 0;
	}
	@Contract(pure = true)
	public static int getColor(boolean pushing) {
		return pushing ? CCG.config.outliner.outColor : CCG.config.outliner.inColor;
	}
	public static double getOffsetScale(int i, int numberOfFlowBoxes) {
		return (System.currentTimeMillis() + i * (3000D / numberOfFlowBoxes)) % 3000 / 3000.0;
	}
	@Contract(pure = true)
	public static int getNumberOfFlowBoxes(float range) {
		return (int) (Math.log(range) + 1);
	}
}
