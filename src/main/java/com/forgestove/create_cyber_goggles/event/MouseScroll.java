package com.forgestove.create_cyber_goggles.event;
import com.forgestove.create_cyber_goggles.CCG;
import com.forgestove.create_cyber_goggles.util.Common;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent;
public class MouseScroll {
	public static void onMouseScroll(MouseScrollingEvent event) {
		if (!CCG.CONFIG.goggles.enhancedStoreRender) return;
		if (Common.getSelectedBE() instanceof TableClothBlockEntity tcbe && tcbe.isShop()) {
			if (event.getScrollDeltaY() == 0) Common.scrollDeltaY = 0;
			else Common.scrollDeltaY = event.getScrollDeltaY() > 0 ? -1 : 1;
			event.setCanceled(true);
		} else Common.index = 1;
	}
}
