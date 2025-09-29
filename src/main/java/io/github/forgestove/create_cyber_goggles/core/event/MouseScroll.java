package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraftforge.client.event.InputEvent.MouseScrollingEvent;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.getBE;
public class MouseScroll {
	public static int index = 1, scrollDeltaY;
	public static void onMouseScroll(MouseScrollingEvent event) {
		if (!CCG.CONFIG.goggles.betterStoreInfo) return;
		if (getBE() instanceof TableClothBlockEntity tcbe && tcbe.isShop()) {
			if (event.getScrollDelta() == 0) scrollDeltaY = 0;
			else scrollDeltaY = event.getScrollDelta() > 0 ? -1 : 1;
			event.setCanceled(true);
		} else index = 1;
	}
}
