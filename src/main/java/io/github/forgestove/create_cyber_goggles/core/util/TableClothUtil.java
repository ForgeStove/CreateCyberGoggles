package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import io.github.forgestove.create_cyber_goggles.core.api.Index;
import io.github.forgestove.create_cyber_goggles.core.event.KeyInput;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.*;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public final class TableClothUtil {
	public static void clothStoreOverlay(GuiGraphics gui, int x, int y, @NotNull List<ItemStack> items) {
		var tcbe = getBlockEntity(TableClothBlockEntity.class);
		if (tcbe == null) return;
		if (items.isEmpty()) {
			AllGuiTextures.HOTSLOT.render(gui, x, y);
			GuiGameElement.of(Items.BARRIER).at(x + 3, y + 3).render(gui);
			return;
		}
		var index = getIndex(items, tcbe);
		if (index == null) return;
		var selectedX = x + index * 21;
		AllGuiTextures.HOTSLOT_SUPER_ACTIVE.render(gui, selectedX - 1, y - 1);
		var resultX = x;
		for (var item : items) {
			BlueprintOverlayRenderer.drawItemStack(gui, mc, resultX, y, item, null);
			resultX += 21;
		}
	}
	public static void tableOverlay(@NotNull GuiGraphics gui) {
		var tcbe = getBlockEntity(TableClothBlockEntity.class);
		if (tcbe == null) return;
		if (tcbe.isShop()) return;
		var items = getItems(tcbe);
		if (items.isEmpty()) return;
		var itemWidth = 21 * items.size();
		var width = gui.guiWidth();
		var x = (width - itemWidth) / 2;
		var height = gui.guiHeight();
		var y = height - 100;
		TooltipRenderUtil.renderTooltipBackground(gui, x, y, itemWidth, 20, 0, 0x55000000, 0x55000000, 0, 0);
		var index = getIndex(items, tcbe);
		if (index == null) return;
		AllGuiTextures.HOTSLOT_SUPER_ACTIVE.render(gui, x + 2 + index * 21 - 4, y - 2);
		for (var i = 0; i < items.size(); i++) {
			var item = items.get(i);
			var itemX = x + 2 + i * 21;
			gui.renderItem(item, itemX, y + 2);
			gui.renderItemDecorations(mc.font, item, itemX, y + 2);
		}
	}
	@Contract("_, null -> null")
	public static @Nullable Integer getIndex(@NotNull List<ItemStack> items, TableClothBlockEntity tcbe) {
		if (!(tcbe instanceof Index i)) return null;
		var index = i.ccg$getIndex() - KeyInput.scrollDeltaY;
		KeyInput.scrollDeltaY = 0;
		var size = items.size();
		if (index < 0) index = size - 1;
		else if (index >= size) index = 0;
		i.ccg$setIndex(index);
		return index;
	}
	public static List<ItemStack> getItems(@NotNull TableClothBlockEntity tcbe) {
		return tcbe.isShop()
			? tcbe.requestData.encodedRequest().stacks().stream().map(bigItemStack -> bigItemStack.stack).toList()
			: tcbe.manuallyAddedItems;
	}
}
