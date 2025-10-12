package io.github.forgestove.create_cyber_goggles.core.event;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.IOutlineRenderable;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import org.jetbrains.annotations.*;

import java.util.Map;
import java.util.Map.Entry;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class OutlineRenderer {
	public static final Map<BlockEntity, Integer> cachedBE = new Object2IntOpenHashMap<>();
	public static void tick(ClientTickEvent ignoredEvent) {
		if (!CCG.CONFIG.outlineRenderer.renderAnalogBox) return;
		if (mc.isPaused() || isInGUI()) return;
		var be = getBlockEntity();
		if (be instanceof IOutlineRenderable) cachedBE.put(be, CCG.CONFIG.outlineRenderer.delayRenderDuration);
		if (cachedBE.isEmpty()) return;
		cachedBE.entrySet().removeIf(OutlineRenderer::render);
	}
	private static boolean render(@NotNull Entry<BlockEntity, Integer> entry) {
		var nextDelay = entry.getValue() - 1;
		entry.setValue(nextDelay);
		var be = entry.getKey();
		if (!be.isRemoved() && be instanceof IOutlineRenderable ior) ior.ccg$render();
		return nextDelay <= 0;
	}
	@Contract(pure = true)
	public static int getColor(boolean pushing) {
		return pushing ? CCG.CONFIG.outlineRenderer.windPushColor : CCG.CONFIG.outlineRenderer.windPullColor;
	}
	public static double getOffset(int i, int numberOfFlowBoxes) {
		return (System.currentTimeMillis() + i * ((double) 3000 / numberOfFlowBoxes)) % 3000 / 3000.0;
	}
	@Contract(pure = true)
	public static int getNumberOfFlowBoxes(float range) {
		return (int) (Math.log(range) + 1);
	}
}
