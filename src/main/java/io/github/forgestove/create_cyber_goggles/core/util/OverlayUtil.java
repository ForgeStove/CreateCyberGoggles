package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.logistics.tableCloth.*;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import io.github.forgestove.create_cyber_goggles.core.event.KeyInput;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public class OverlayUtil {
	public static void clothStoreOverlay(
		GuiGraphics graphics,
		int x,
		int y,
		boolean invalidShop,
		@NotNull List<ItemStack> items,
		boolean resultCraftable,
		TableClothBlockEntity tcbe,
		BlueprintOverlayShopContext shopContext
	) {
		if (items.isEmpty()) {
			AllGuiTextures.HOTSLOT.render(graphics, x, y);
			GuiGameElement.of(Items.BARRIER).at(x + 3, y + 3).render(graphics);
			return;
		}
		if (!(tcbe instanceof IIndex iIndex)) return;
		var index = iIndex.ccg$getIndex() - KeyInput.scrollDeltaY;
		KeyInput.scrollDeltaY = 0;
		var size = items.size();
		if (index < 0) index = size - 1;
		else if (index >= size) index = 0;
		iIndex.ccg$setIndex(index);
		var selectedX = x + index * 21;
		var resultX = x;
		for (var item : items) {
			var slot = getGuiTextures(invalidShop, resultCraftable, shopContext, resultX, selectedX);
			if (slot == AllGuiTextures.HOTSLOT_SUPER_ACTIVE) slot.render(graphics, resultX - 1, y - 1);
			else slot.render(graphics, resultX, y);
			BlueprintOverlayRenderer.drawItemStack(graphics, mc, resultX, y, item, null);
			resultX += 21;
		}
	}
	public static @NotNull AllGuiTextures getGuiTextures(
		boolean invalidShop,
		boolean resultCraftable,
		BlueprintOverlayShopContext shopContext,
		int resultX,
		int selectedX
	) {
		if (resultX == selectedX) return AllGuiTextures.HOTSLOT_SUPER_ACTIVE;
		var isActive = !invalidShop && shopContext != null && shopContext.stockLevel() > shopContext.purchases();
		if (isActive) return AllGuiTextures.HOTSLOT_ACTIVE;
		if (resultCraftable) return AllGuiTextures.HOTSLOT_SUPER_ACTIVE;
		return AllGuiTextures.HOTSLOT;
	}
}
