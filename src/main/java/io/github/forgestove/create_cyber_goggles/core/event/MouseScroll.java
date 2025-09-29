package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.getBlockEntity;
public class MouseScroll {
	public static int index = 1, scrollDeltaY;
	public static void tick(MouseScrollingEvent event) {
		if (!CCG.CONFIG.goggles.betterStoreInfo) return;
		var tcbe = getBlockEntity(TableClothBlockEntity.class);
		if (tcbe != null && tcbe.isShop()) {
			if (event.getScrollDeltaY() == 0) scrollDeltaY = 0;
			else scrollDeltaY = event.getScrollDeltaY() > 0 ? -1 : 1;
			event.setCanceled(true);
		} else index = 1;
	}
}
