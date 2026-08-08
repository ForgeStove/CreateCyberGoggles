package io.github.forgestove.create_cyber_goggles.mixin.misc.jei;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts.CraftingEntry;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.createmod.catnip.data.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;
import java.util.function.Function;
@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperRequestScreenMixin extends AbstractSimiContainerScreen<StockKeeperRequestMenu>
	implements Self<StockKeeperRequestScreen> {
	/** 普通请求模式：由界面右下角按钮切换，static 缓存跨界面共享 */
	@Unique private static boolean ccg$plainRequest;
	@Shadow int windowWidth, windowHeight;
	public StockKeeperRequestScreenMixin(StockKeeperRequestMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	/** 仓库管理员发包去掉 ordered_crafts，使理包机不吞物品 */
	@ModifyArg(
		method = "sendIt", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrderWithCrafts;<init>("
			+ "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrder;Ljava/util/List;)V"
	), index = 1
	)
	private List<CraftingEntry> plainRequest(List<CraftingEntry> orderedCrafts) {
		if (!CCG.config.misc.jei.allowLargeCrafting) return orderedCrafts;
		return ccg$plainRequest ? List.of() : orderedCrafts;
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
		if (!CCG.config.misc.jei.allowLargeCrafting || !ccg$plainRequest) return orderedStacks;
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
	/** 在右下角添加动力合成切换按钮（复用工厂仪表动力合成按钮样式与 tooltip） */
	@Inject(method = "init", at = @At("TAIL"))
	private void ccg$addPlainRequestButton(CallbackInfo ci) {
		if (!CCG.config.misc.jei.allowLargeCrafting) return;
		var thiz = thiz();
		var button = new IconButton(thiz.getGuiLeft() + windowWidth - 29, thiz.getGuiTop() + windowHeight - 21, AllIcons.I_3x3);
		button.green = !ccg$plainRequest;
		button.withCallback(() -> {
			ccg$plainRequest = !ccg$plainRequest;
			button.green = !ccg$plainRequest;
		});
		button.setToolTip(Component.translatable("create.gui.factory_panel.activate_crafting"));
		addRenderableWidget(button);
	}
}
