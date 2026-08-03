package io.github.forgestove.create_cyber_goggles.mixin.misc.jei;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.compat.jei.StockKeeperTransferHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.common.transfer.RecipeTransferOperationsResult;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

import java.util.*;
/** 让仓库请求界面支持动力合成配方（原料数 10~81） */
@Mixin(StockKeeperTransferHandler.class)
public abstract class StockKeeperTransferHandlerMixin {
	@ModifyConstant(method = "transferRecipeOnClient", constant = @Constant(intValue = 9))
	private int transferRecipeOnClient(int constant) {
		return CCG.config.misc.jei.allowLargeCrafting ? 81 : constant;
	}
	/** 网络物品过多时 getRecipeTransferOperations 全量遍历会卡顿；只传配方相关物品 */
	@WrapOperation(
		method = "transferRecipeOnClient", at = @At(
		value = "INVOKE",
		target = "Lmezz/jei/common/transfer/RecipeTransferUtil;getRecipeTransferOperations("
			+ "Lmezz/jei/api/helpers/IStackHelper;Ljava/util/Map;Ljava/util/List;Ljava/util/List;)"
			+ "Lmezz/jei/common/transfer/RecipeTransferOperationsResult;"
	)
	)
	private RecipeTransferOperationsResult filterAvailableStacks(
		IStackHelper helper,
		Map<Slot, ItemStack> availableItemStacks,
		List<IRecipeSlotView> required,
		List<Slot> crafting,
		Operation<RecipeTransferOperationsResult> original,
		@Local(argsOnly = true) RecipeHolder<Recipe<?>> recipeHolder
	) {
		if (!CCG.config.misc.jei.optimizeRecipeProcessing) return original.call(helper, availableItemStacks, required, crafting);
		var items = ccg$recipeItems(recipeHolder.value());
		if (items.isEmpty()) return original.call(helper, availableItemStacks, required, crafting);
		Map<Slot, ItemStack> filtered = new HashMap<>();
		for (var e : availableItemStacks.entrySet())
			if (items.contains(e.getValue().getItem())) filtered.put(e.getKey(), e.getValue());
		return original.call(helper, filtered, required, crafting);
	}
	@Unique
	private Set<Item> ccg$recipeItems(Recipe<?> recipe) {
		Set<Item> items = new HashSet<>();
		for (var ingredient : recipe.getIngredients()) {
			if (ingredient.isEmpty()) continue;
			for (var match : ingredient.getItems()) items.add(match.getItem());
		}
		return items;
	}
}
