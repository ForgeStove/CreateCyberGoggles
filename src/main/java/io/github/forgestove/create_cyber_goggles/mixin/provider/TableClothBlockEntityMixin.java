package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(TableClothBlockEntity.class)
public abstract class TableClothBlockEntityMixin implements IItemRenderable, IItemIndex {
	@Unique public int ccg$index;
	@Override
	public ItemStack ccg$getItemStack() {
		var tcbe = (TableClothBlockEntity) (Object) this;
		var isShop = tcbe.isShop();
		var items = isShop
			? tcbe.requestData.encodedRequest.stacks().stream().map(bigItemStack -> bigItemStack.stack).toList()
			: tcbe.manuallyAddedItems;
		if (items.isEmpty()) return null;
		if (!isShop) return items.get(items.size() - 1);
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
