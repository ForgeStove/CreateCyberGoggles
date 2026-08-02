package io.github.forgestove.create_cyber_goggles.mixin.misc.jei;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts.CraftingEntry;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.Self;
import net.createmod.catnip.data.Pair;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.Function;
@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperRequestScreenMixin implements Self<StockKeeperRequestScreen> {
	/** 仓库管理员发包去掉 ordered_crafts，使理包机不吞物品 */
	@ModifyArg(
		method = "sendIt", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrderWithCrafts;<init>("
			+ "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrder;Ljava/util/List;)V"
	), index = 1
	)
	private List<CraftingEntry> plainRequest(List<CraftingEntry> orderedCrafts) {
		return CCG.config.misc.jei.plainRequest ? List.of() : orderedCrafts;
	}
	/** 普通请求模式下 ordered_stacks 按配方连续组生成（与红石请求器一致，理包机输出有序） */
	@ModifyArg(
		method = "sendIt", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrderWithCrafts;<init>("
			+ "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrder;Ljava/util/List;)V"
	), index = 0
	)
	private PackageOrder orderedByRecipe(PackageOrder orderedStacks) {
		if (!CCG.config.misc.jei.plainRequest) return orderedStacks;
		var groups = ccg$recipeGroups();
		return groups.isEmpty() ? orderedStacks : new PackageOrder(groups);
	}
	@Unique
	private List<BigItemStack> ccg$recipeGroups() {
		List<BigItemStack> groups = new ArrayList<>();
		BigItemStack currentGroup = null;
		var thiz = thiz();
		for (var cbis : thiz.recipesToOrder) {
			if (!(cbis.recipe instanceof CraftingRecipe cr)) continue;
			for (var ingredient : cr.getIngredients()) {
				if (ingredient.isEmpty()) continue;
				// 优先用实际请求的物品（与包裹内容一致，避免理包机按 tag 首个物品匹配不上而漏物品）
				ItemStack rep = null;
				for (var ordered : thiz.itemsToOrder) {
					if (!ingredient.test(ordered.stack)) continue;
					rep = ordered.stack;
					break;
				}
				if (rep == null) {
					var matches = ingredient.getItems();
					if (matches.length > 0) rep = matches[0];
				}
				if (rep == null) continue;
				if (currentGroup != null && ItemStack.isSameItemSameComponents(currentGroup.stack, rep)) currentGroup.count++;
				else {
					currentGroup = new BigItemStack(rep.copyWithCount(1), 1);
					groups.add(currentGroup);
				}
			}
		}
		return groups;
	}
	/** 网络物品过多时 maxCraftable 遍历全量会卡顿；把快照过滤为配方相关物品再计算 */
	@WrapOperation(
		method = "requestCraftable", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/content/logistics/stockTicker/StockKeeperRequestScreen;maxCraftable("
			+ "Lcom/simibubi/create/content/logistics/stockTicker/CraftableBigItemStack;"
			+ "Lcom/simibubi/create/content/logistics/packager/InventorySummary;"
			+ "Ljava/util/function/Function;I)"
			+ "Lnet/createmod/catnip/data/Pair;"
	)
	)
	private Pair<Integer, List<List<BigItemStack>>> filterMaxCraftable(
		StockKeeperRequestScreen instance,
		CraftableBigItemStack cbis,
		InventorySummary availableItems,
		Function<ItemStack, Integer> countModifier,
		int newTypeLimit,
		Operation<Pair<Integer, List<List<BigItemStack>>>> original
	) {
		if (!CCG.config.misc.jei.optimizeRecipeProcessing)
			return original.call(instance, cbis, availableItems, countModifier, newTypeLimit);
		Set<Item> items = new HashSet<>();
		for (var ingredient : cbis.getIngredients()) {
			if (ingredient.isEmpty()) continue;
			for (var match : ingredient.getItems()) items.add(match.getItem());
		}
		if (items.isEmpty()) return original.call(instance, cbis, availableItems, countModifier, newTypeLimit);
		var filtered = new InventorySummary();
		for (var item : items) {
			var list = availableItems.getItemMap().get(item);
			if (list == null) continue;
			for (var entry : list) filtered.add(entry.stack, entry.count);
		}
		return original.call(instance, cbis, filtered, countModifier, newTypeLimit);
	}
	/** create 的 resolveIngredientAmounts 按数量逐 1 递减，物品数量巨大时 O(总量) 卡顿；改为按共享引用数一次分摊 */
	@Inject(method = "resolveIngredientAmounts", at = @At("HEAD"), cancellable = true)
	private void fastResolve(List<List<BigItemStack>> validIngredients, CallbackInfoReturnable<List<List<BigItemStack>>> cir) {
		if (!CCG.config.misc.jei.optimizeRecipeProcessing) return;
		Map<BigItemStack, Integer> shareCount = new IdentityHashMap<>();
		for (var list : validIngredients)
			for (BigItemStack entry : list)
				shareCount.merge(entry, 1, Integer::sum);
		List<List<BigItemStack>> resolved = new ArrayList<>(validIngredients.size());
		for (var list : validIngredients) {
			List<BigItemStack> resolvedList = new ArrayList<>(list.size());
			for (var entry : list)
				resolvedList.add(new BigItemStack(entry.stack, entry.count / shareCount.get(entry)));
			resolved.add(resolvedList);
		}
		cir.setReturnValue(resolved);
	}
}
