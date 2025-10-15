package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.logistics.redstoneRequester.AutoRequestData;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;

import java.util.List;
@Mixin(TableClothBlockEntity.class)
public abstract class TableClothBlockEntityMixin implements IItemRenderable, IItemIndex {
	@Unique public int ccg$index;
	@Shadow public AutoRequestData requestData;
	@Shadow public List<ItemStack> manuallyAddedItems;
	@Shadow
	public abstract boolean isShop();
	@Override
	public ItemStack ccg$getItemStack() {
		List<ItemStack> items;
		if (!isShop()) items = manuallyAddedItems;
		else items = requestData.encodedRequest().stacks().stream().map(bigItemStack -> bigItemStack.stack).toList();
		if (items.isEmpty()) return null;
		if (!isShop()) return items.getLast();
		if (ccg$index >= items.size()) ccg$index = 0;
		return items.get(ccg$index);
	}
	@Override
	public int ccg$getIndex() {
		return ccg$index;
	}
	@Override
	public void ccg$setIndex(int index) {
		ccg$index = index;
	}
}
