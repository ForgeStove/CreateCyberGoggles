package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import io.github.forgestove.create_cyber_goggles.core.util.TableClothUtil;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(TableClothBlockEntity.class)
public abstract class TableClothBlockEntityMixin implements ItemRenderable, Index, Self<TableClothBlockEntity> {
	@Unique public int ccg$index;
	@Override
	public ItemStack ccg$getItemStack() {
		var items = TableClothUtil.getItems(self());
		if (items.isEmpty()) return null;
		if (!CCG.config.goggles.betterStoreInfo) return null;
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
