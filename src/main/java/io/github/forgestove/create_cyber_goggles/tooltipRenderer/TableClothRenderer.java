package io.github.forgestove.create_cyber_goggles.tooltipRenderer;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.redstoneRequester.AutoRequestData;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.AutoTooltipRenderer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
@AutoTooltipRenderer
public final class TableClothRenderer extends AbstractItemGridRenderer {
	@Override
	public boolean supports(ItemStack stack) {
		return CCG.config.tooltip.tableCloth && stack.getItem() instanceof TableClothBlockItem;
	}
	@Override
	public @Nullable OverlayData buildItemGrid(ItemStack stack) {
		AutoRequestData data = stack.get(AllDataComponents.AUTO_REQUEST_DATA);
		if (data == null) return null;
		List<BigItemStack> bigStacks = data.encodedRequest().stacks();
		if (bigStacks.isEmpty()) return null;
		var stacks = new ArrayList<ItemStack>();
		bigStacks.forEach(bigStack -> stacks.add(bigStack.stack.copyWithCount(bigStack.count)));
		return new OverlayData(stacks, 3);
	}
}
