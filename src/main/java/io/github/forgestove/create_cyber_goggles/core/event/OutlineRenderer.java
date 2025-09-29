package io.github.forgestove.create_cyber_goggles.core.event;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.IOutlineRenderable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import org.jetbrains.annotations.Contract;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class OutlineRenderer {
	public static final Map<BlockEntity, Integer> cachedBE = new HashMap<>();
	public static void tick(Post ignoredEvent) {
		if (!CCG.CONFIG.outlineRenderer.renderAnalogBox) return;
		if (mc.level == null) {
			cachedBE.clear();
			return;
		}
		if (mc.isPaused() || isInGUI()) return;
		var be = getBlockEntity();
		if (be instanceof IOutlineRenderable) cachedBE.put(be, CCG.CONFIG.outlineRenderer.delayRenderDuration);
		if (cachedBE.isEmpty()) return;
		cachedBE.entrySet().removeIf(entry -> {
			var newValue = entry.getValue() - 1;
			entry.setValue(newValue);
			var key = entry.getKey();
			if (!key.isRemoved() && key instanceof IOutlineRenderable ior) ior.ccg$render();
			return newValue <= 0;
		});
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
