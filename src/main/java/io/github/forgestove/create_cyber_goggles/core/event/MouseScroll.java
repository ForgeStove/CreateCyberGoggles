package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGUtil;
import net.minecraftforge.client.event.InputEvent.MouseScrollingEvent;
public class MouseScroll {
	public static int index = 1, scrollDeltaY;
	public static void onMouseScroll(MouseScrollingEvent event) {
		if (!CCG.CONFIG.goggles.betterStoreInfo) return;
		if (CCGUtil.getBE() instanceof TableClothBlockEntity tcbe && tcbe.isShop()) {
			if (event.getScrollDelta() == 0) scrollDeltaY = 0;
			else scrollDeltaY = event.getScrollDelta() > 0 ? -1 : 1;
			event.setCanceled(true);
		} else index = 1;
	}
}
