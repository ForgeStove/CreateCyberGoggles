package io.github.forgestove.create_cyber_goggles.event;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import io.github.forgestove.create_cyber_goggles.*;
import net.minecraft.client.gui.screens.Screen;
public class MouseScroll {
	public static boolean onMouseScroll(
		Screen ignoredScreen,
		double ignoredMouseX,
		double ignoredMouseY,
		double horizontalAmount,
		double ignoredVerticalAmount
	) {
		if (!CCG.CONFIG.goggles.betterStoreInfo) return false;
		if (Common.getBE() instanceof TableClothBlockEntity tcbe && tcbe.isShop()) {
			if (horizontalAmount == 0) Common.scrollDeltaY = 0;
			else Common.scrollDeltaY = horizontalAmount > 0 ? -1 : 1;
			return false;
		} else Common.index = 1;
		return true;
	}
}
