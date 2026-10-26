package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import io.github.forgestove.create_cyber_goggles.core.event.KeyInput;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Index;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.*;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public final class TableClothUtil {
	public static void clothStoreOverlay(GuiGraphics gui, int x, int y, @NotNull List<ItemStack> items) {
		TableClothBlockEntity tcbe = getBlockEntity(TableClothBlockEntity.class);
		if (tcbe == null) return;
		if (items.isEmpty()) {
			AllGuiTextures.HOTSLOT.render(gui, x, y);
			GuiGameElement.of(Items.BARRIER).at(x + 3, y + 3).render(gui);
			return;
		}
		Integer index = getIndex(items, tcbe);
		if (index == null) return;
		int selectedX = x + index * 21;
		AllGuiTextures.HOTSLOT_SUPER_ACTIVE.render(gui, selectedX - 1, y - 1);
		int resultX = x;
		for (ItemStack item : items) {
			BlueprintOverlayRenderer.drawItemStack(gui, mc, resultX, y, item, null);
			resultX += 21;
		}
	}
	@Contract("_, null -> null")
	public static @Nullable Integer getIndex(@NotNull List<ItemStack> items, TableClothBlockEntity tcbe) {
		if (!(tcbe instanceof Index i)) return null;
		int index = i.ccg$getIndex() - KeyInput.scrollDeltaY;
		KeyInput.scrollDeltaY = 0;
		int size = items.size();
		if (index < 0) index = size - 1;
		else if (index >= size) index = 0;
		i.ccg$setIndex(index);
		return index;
	}
	public static void tableOverlay(@NotNull GuiGraphics gui) {
		TableClothBlockEntity tcbe = getBlockEntity(TableClothBlockEntity.class);
		if (tcbe == null) return;
		if (tcbe.isShop()) return;
		List<ItemStack> items = getItems(tcbe);
		if (items.isEmpty()) return;
		int itemWidth = 21 * items.size();
		int width = gui.guiWidth();
		int x = (width - itemWidth) / 2;
		int height = gui.guiHeight();
		int y = height - 100;
		TooltipRenderUtil.renderTooltipBackground(gui, x, y, itemWidth, 20, 0, 0x55000000, 0x55000000, 0, 0);
		Integer index = getIndex(items, tcbe);
		if (index == null) return;
		AllGuiTextures.HOTSLOT_SUPER_ACTIVE.render(gui, x + 2 + index * 21 - 4, y - 2);
		for (var i = 0; i < items.size(); i++) {
			ItemStack item = items.get(i);
			int itemX = x + 2 + i * 21;
			gui.renderItem(item, itemX, y + 2);
			gui.renderItemDecorations(mc.font, item, itemX, y + 2);
		}
	}
	public static List<ItemStack> getItems(@NotNull TableClothBlockEntity tcbe) {
		return tcbe.isShop()
			? tcbe.requestData.encodedRequest().stacks().stream().map(bigItemStack -> bigItemStack.stack).toList()
			: tcbe.manuallyAddedItems;
	}
}
