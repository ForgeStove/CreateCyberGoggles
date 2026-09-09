package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.factory.*;
import io.github.forgestove.create_cyber_goggles.core.factory.ReplenishGroup.*;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.Map.Entry;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
/**
 * 在 Create {@link StockKeeperRequestScreen} 右下角加「自动补齐缺货」。
 * 需求来源 = 剪贴板目标成品（{@link ClipboardEntry} icon+itemAmount）。
 * <b>递归到原材料</b>，三阶段求解，保证发出的配方原料一定齐备：
 * ① 自顶向下展开依赖图（一物品一节点，防环）；② 自底向上算「产能上限」——父节点的可合成次数
 * 受<b>子节点实际产出</b>限制（不再把可合成原料当无限制）；③ 自顶向下按父的实际消耗分配需求，
 * 取 min(需求次数, 产能上限) 作为最终 craftTimes。共享原料按序消耗。只读。
 */
@Mixin(value = StockKeeperRequestScreen.class, remap = false)
public abstract class StockKeeperReplenishEntryMixin extends AbstractSimiContainerScreen<StockKeeperRequestMenu>
	implements Self<StockKeeperRequestScreen> {
	@Unique private final Map<Item, Recipe<?>> ccg$recipeCache = new HashMap<>();
	@Unique private Set<Item> ccg$recipeOutputs;
	@Shadow List<List<ClipboardEntry>> clipboardItem;
	@Shadow StockTickerBlockEntity blockEntity;
	/** 缺失物品（全局一块）：找不到配方的需求 + 不可合成原料的缺口，数量 = 还差多少个 */
	@Unique private List<ItemStack> ccg$missing = List.of();
	public StockKeeperReplenishEntryMixin(StockKeeperRequestMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	@Inject(method = "init", at = @At("TAIL"))
	private void ccg$addAutoReplenishButton(CallbackInfo ci) {
		var btn = new IconButton(leftPos + imageWidth - 22, topPos + 4, AllIcons.I_ADD);
		btn.withCallback(() -> {
			List<ReplenishGroup> groups = ccg$buildGroups();
			var summary = blockEntity.getLastClientsideStockSnapshotAsSummary();
			CCG.LOGGER.info("ccg autoReplenish click: groups={} summary={}", groups.size(), summary == null ? "null" : "ok");
			mc.setScreen(new AutoReplenishScreen(thiz(), blockEntity, groups, ccg$missing));
		});
		btn.setToolTip(Component.translatable("create_cyber_goggles.gui.auto_replenish.title"));
		addRenderableWidget(btn);
	}
	@Unique
	private List<ReplenishGroup> ccg$buildGroups() {
		InventorySummary summary = blockEntity.getLastClientsideStockSnapshotAsSummary();
		if (summary == null || mc.level == null || clipboardItem == null) return List.of();
		// 1) 根需求（裸 Item 键：ItemStack 的 equals 含 count/组件，同物品会被判成多个键）
		Map<Item, Integer> rootNeed = new LinkedHashMap<>();
		for (List<ClipboardEntry> page : clipboardItem)
			for (ClipboardEntry entry : page)
				if (!entry.icon.isEmpty() && entry.itemAmount > 0) rootNeed.merge(entry.icon.getItem(), entry.itemAmount, Integer::sum);
		if (rootNeed.isEmpty()) return List.of();
		// 2) 自顶向下展开依赖图：一物品一节点，缺多少记在 shortfall。
		//    需求单调递增——同一物品可能先以较小需求建了节点，之后又被另一个父节点加需求，
		//    所以按「上次传播时的 want」做差量补发（不能用已展开标记挡住，否则后到的需求会丢）。
		//    环会让需求不断放大，用轮次上限兜底。
		Map<Item, Draft> drafts = new LinkedHashMap<>();
		Map<Item, Integer> pending = new LinkedHashMap<>(rootNeed);
		Map<Item, Integer> spreadWant = new HashMap<>();
		for (var round = 0; round < 32; round++) {
			var progressed = false;
			for (var itemId : new ArrayList<>(pending.keySet())) {
				int needed = pending.getOrDefault(itemId, 0);
				if (needed <= 0) continue;
				ItemStack item = itemId.getDefaultInstance();
				int have = summary.getCountOf(item);
				if (have >= needed) continue;                       // 库存已够
				Recipe<?> recipe = ccg$findAnyRecipeFor(item);
				if (recipe == null) continue;                       // 原材料
				List<DraftItem> items = ccg$draftItems(recipe, summary);
				if (items.isEmpty()) continue;
				int outPer = Math.max(1, recipe.getResultItem(mc.level.registryAccess()).getCount());
				int want = (needed - have + outPer - 1) / outPer;
				Integer prev = spreadWant.get(itemId);
				if (prev != null && prev == want) continue;         // 需求没变，已传播过
				drafts.put(itemId, new Draft(item, recipe, outPer, want, needed - have, items));
				spreadWant.put(itemId, want);
				progressed = true;
				CCG.LOGGER.info(
					"ccg autoReplenish expand: {} need={} have={} → want={} (单次产出 {})",
					item.getHoverName().getString(),
					needed,
					have,
					want,
					outPer
				);
				// 只补差额，避免重复累加
				int delta = want - (prev == null ? 0 : prev);
				for (DraftItem di : items) {
					Item matItem = di.material().getItem();
					if (ccg$findAnyRecipeFor(di.material()) == null) continue;
					pending.merge(matItem, di.per() * delta, Integer::sum);
				}
			}
			if (!progressed) break;
		}
		// 注意：drafts 为空也要继续走完——剪贴板里的物品全都没有配方时，它们必须出现在缺失列表里
		// 3) 最长路径深度：保证「父 < 子」，深度倒序即「子先父后」的拓扑序
		Map<Item, Integer> depth = ccg$depths(drafts, rootNeed.keySet());
		List<Item> deepFirst = new ArrayList<>(drafts.keySet());
		deepFirst.sort(Comparator.comparingInt((Item i) -> depth.getOrDefault(i, 0)).reversed());
		// 4) 自底向上：产能上限 cap —— 受缺口需求与原料可获得量双重限制，共享原料按序消耗。
		//    「可获得量」对可合成原料取它自己的产出池（未算出=0），所以父节点永远不会超过子节点的实际产能。
		Map<Item, Integer> pool = new HashMap<>();
		for (Item itemId : drafts.keySet()) pool.put(itemId, 0);
		Map<Item, Integer> cap = new HashMap<>();
		Map<Item, Integer> missing = new LinkedHashMap<>();
		for (Item itemId : deepFirst) {
			Draft d = drafts.get(itemId);
			int times = d.want();
			Item limited = null;
			var limitedAvail = 0;
			var limitedPer = 0;
			for (DraftItem di : d.items()) {
				if (di.per() <= 0) continue;
				int avail = ccg$avail(pool, di.material(), summary);
				if (avail / di.per() < times) {
					times = avail / di.per();
					limited = di.material().getItem();
					limitedAvail = avail;
					limitedPer = di.per();
				}
				// 缺失原料：不可合成（无节点）且扣掉前面节点消耗后仍不够目标次数 → 记缺口
				int need = di.per() * d.want();
				if (!drafts.containsKey(di.material().getItem()) && avail < need)
					missing.merge(di.material().getItem(), need - avail, Integer::sum);
			}
			times = Math.max(0, times);
			if (limited != null) CCG.LOGGER.info(
				"ccg autoReplenish cap: {} 受限于 {} (可用 {} / 每次 {}) → cap={}",
				d.target().getHoverName().getString(),
				limited.getDefaultInstance().getHoverName().getString(),
				limitedAvail,
				limitedPer,
				times
			);
			for (DraftItem di : d.items()) ccg$consume(pool, di.material(), di.per() * times);
			pool.merge(itemId, times * d.outPer(), Integer::sum);
			cap.put(itemId, times);
		}
		// 根需求里「找不到配方」（没有节点）却库存不足的物品：直接列入缺失
		for (Entry<Item, Integer> e : rootNeed.entrySet()) {
			if (drafts.containsKey(e.getKey())) continue;
			int have = summary.getCountOf(e.getKey().getDefaultInstance());
			if (have < e.getValue()) missing.merge(e.getKey(), e.getValue() - have, Integer::sum);
		}
		// 5) 自顶向下：实际合成次数 —— 需求由父节点的实际消耗累加（不再按目标次数虚高），且不超过产能上限
		Map<Item, Integer> needItems = new HashMap<>();
		for (Entry<Item, Integer> e : rootNeed.entrySet()) {
			Draft d = drafts.get(e.getKey());
			if (d != null) needItems.merge(e.getKey(), d.shortfall(), Integer::sum);
		}
		Map<Item, Integer> times = new HashMap<>();
		List<Item> shallowFirst = new ArrayList<>(deepFirst);
		Collections.reverse(shallowFirst);
		for (Item itemId : shallowFirst) {
			Draft d = drafts.get(itemId);
			int need = needItems.getOrDefault(itemId, 0);
			int t = Math.min((need + d.outPer() - 1) / d.outPer(), cap.getOrDefault(itemId, 0));
			times.put(itemId, t);
			if (t <= 0) continue;
			for (DraftItem di : d.items()) {
				Item matItem = di.material().getItem();
				if (!drafts.containsKey(matItem)) continue;
				needItems.merge(matItem, di.per() * t, Integer::sum);
			}
		}
		// 6) 组装节点并按配方类型分组（缺失原料已在步骤 4 统计好，含 craftTimes=0 的节点）
		Map<String, List<Node>> byType = new LinkedHashMap<>();
		for (Draft d : drafts.values()) {
			Item itemId = d.target().getItem();
			int t = times.getOrDefault(itemId, 0);
			String type = d.recipe().getType().toString();
			byType.computeIfAbsent(type, k -> new ArrayList<>());
			if (t <= 0) continue;
			List<ReplenishEntry> entries = new ArrayList<>(d.items().size());
			for (DraftItem di : d.items()) {
				int stock = summary.getCountOf(di.material());
				entries.add(new ReplenishEntry(di.material(), di.per(), stock >= di.per(), drafts.containsKey(di.material().getItem())));
			}
			int nodeDepth = depth.getOrDefault(itemId, 0);
			byType.get(type).add(new Node(d.target().copy(), d.want(), d.recipe(), t, entries, nodeDepth));
			CCG.LOGGER.info(
				"ccg autoReplenish node: {} craft={}/{} depth={}",
				d.target().getHoverName().getString(),
				t,
				d.want(),
				nodeDepth
			);
		}
		List<ReplenishGroup> out = new ArrayList<>();
		for (Entry<String, List<Node>> e : byType.entrySet()) out.add(new ReplenishGroup(e.getKey(), e.getValue()));
		List<ItemStack> missingList = new ArrayList<>(missing.size());
		missing.forEach((item, count) -> missingList.add(new ItemStack(item, count)));
		ccg$missing = missingList;
		if (!missingList.isEmpty()) CCG.LOGGER.info("ccg autoReplenish missing: {}", missingList);
		return out;
	}
	@Unique
	private Recipe<?> ccg$findAnyRecipeFor(ItemStack target) {
		if (mc.level == null) return null;
		Item key = target.getItem();
		if (ccg$recipeCache.containsKey(key)) return ccg$recipeCache.get(key);
		var registry = mc.level.registryAccess();
		Recipe<?> best = null;
		var bestTier = -1;
		var bestRaw = -1;
		var bestOut = 0;
		outer:
		for (RecipeHolder<?> holder : mc.level.getRecipeManager().getRecipes()) {
			Recipe<?> recipe = holder.value();
			if (recipe.getIngredients().isEmpty()) continue;
			ItemStack result = recipe.getResultItem(registry);
			if (result.isEmpty() || !ItemStack.isSameItemSameComponents(result, target)) continue;
			// 产物不能同时是自己的原料（自循环配方），跳过
			for (Ingredient ing : recipe.getIngredients())
				if (!ing.isEmpty() && ing.test(target)) {
					CCG.LOGGER.info("    跳过自循环配方 {} (产物 {} 也是原料)", recipe.getType(), target.getHoverName().getString());
					continue outer;
				}
			// 排序优先级：① Create 自带配方（配方 ID 或配方类型属于 create）> 其他；
			// ② 原料更「基础」（原料里没有配方的越多越基础），否则会选中「1 铁块→9 铁锭」
			// 这类逆向配方，与「9 铁锭→1 铁块」互锁 → 整条链 cap=0；③ 单次产出最多
			// （如 1 铁锭→9 铁粒 优于 1 铁锭→1 铁粒）
			var tier = "create".equals(holder.id().getNamespace()) || recipe.getType().toString().startsWith("create:") ? 1 : 0;
			var raw = 0;
			for (Ingredient ing : recipe.getIngredients()) {
				if (ing.isEmpty()) continue;
				var craftable = false;
				for (ItemStack option : ing.getItems())
					if (ccg$hasRecipe(option)) {
						craftable = true;
						break;
					}
				if (!craftable) raw++;
			}
			var better = tier > bestTier || tier == bestTier && (raw > bestRaw || raw == bestRaw && result.getCount() > bestOut);
			if (better) {
				best = recipe;
				bestTier = tier;
				bestRaw = raw;
				bestOut = result.getCount();
			}
		}
		if (best != null)
			CCG.LOGGER.info("    选中配方 {} 单次产出 {}x (基础原料 {} create={})", best.getType(), bestOut, bestRaw, bestTier > 0);
		ccg$recipeCache.put(key, best);
		return best;
	}
	/** 该物品是否是某个配方的产物（只查产物表、不递归，用于给原料的「基础度」打分） */
	@Unique
	private boolean ccg$hasRecipe(ItemStack target) {
		if (ccg$recipeOutputs == null) {
			ccg$recipeOutputs = new HashSet<>();
			if (mc.level != null) for (RecipeHolder<?> holder : mc.level.getRecipeManager().getRecipes()) {
				Recipe<?> recipe = holder.value();
				if (recipe.getIngredients().isEmpty()) continue;
				ItemStack out = recipe.getResultItem(mc.level.registryAccess());
				if (!out.isEmpty()) ccg$recipeOutputs.add(out.getItem());
			}
		}
		return ccg$recipeOutputs.contains(target.getItem());
	}
	/** 单个配方的原料清单：同物品多槽累加 per，material 统一数量 1 */
	@Unique
	private static List<DraftItem> ccg$draftItems(Recipe<?> recipe, InventorySummary summary) {
		Map<ItemStack, Integer> per = new LinkedHashMap<>();
		for (Ingredient ing : recipe.getIngredients()) {
			if (ing.isEmpty()) continue;
			ItemStack pick = ccg$pickFromIngredient(ing, summary);
			if (pick.isEmpty()) continue;
			var merged = false;
			for (Entry<ItemStack, Integer> e : per.entrySet())
				if (ItemStack.isSameItemSameComponents(e.getKey(), pick)) {
					per.put(e.getKey(), e.getValue() + 1);
					merged = true;
					break;
				}
			if (!merged) per.put(pick.copy(), 1);
		}
		List<DraftItem> out = new ArrayList<>(per.size());
		per.forEach((stack, count) -> out.add(new DraftItem(stack.copyWithCount(1), count)));
		return out;
	}
	/** 最长路径深度（父恒 < 子）；环会让深度持续增长，用 guard 兜底 */
	@Unique
	private static Map<Item, Integer> ccg$depths(Map<Item, Draft> drafts, Set<Item> roots) {
		Map<Item, Integer> depth = new HashMap<>();
		for (Item root : roots)
			if (drafts.containsKey(root)) depth.put(root, 0);
		for (var guard = 0; guard < 32; guard++) {
			var changed = false;
			for (Entry<Item, Draft> e : drafts.entrySet()) {
				Integer cur = depth.get(e.getKey());
				if (cur == null) continue;
				for (DraftItem di : e.getValue().items()) {
					Item matItem = di.material().getItem();
					if (!drafts.containsKey(matItem)) continue;
					if (cur + 1 > depth.getOrDefault(matItem, -1)) {
						depth.put(matItem, cur + 1);
						changed = true;
					}
				}
			}
			if (!changed) return depth;
		}
		return depth;
	}
	/** 原料当前可获得量：可合成节点取产出池（未算出=0，绝不误用库存），其余取库存剩余 */
	@Unique
	private static int ccg$avail(Map<Item, Integer> pool, ItemStack material, InventorySummary summary) {
		return pool.computeIfAbsent(material.getItem(), k -> summary.getCountOf(k.getDefaultInstance()));
	}
	/** 扣减已消耗的原料（调用前必先 {@link #ccg$avail} 初始化过，避免 merge 出负值） */
	@Unique
	private static void ccg$consume(Map<Item, Integer> pool, ItemStack material, int amount) {
		if (amount <= 0) return;
		pool.merge(material.getItem(), -amount, Integer::sum);
	}
	@Unique
	private static ItemStack ccg$pickFromIngredient(Ingredient ing, InventorySummary summary) {
		ItemStack best = null;
		var bestCount = 0;
		for (List<BigItemStack> list : summary.getItemMap().values())
			for (BigItemStack entry : list)
				if (ing.test(entry.stack) && entry.count > bestCount) {
					bestCount = entry.count;
					best = entry.stack;
				}
		if (best != null) return best;
		ItemStack[] items = ing.getItems();
		return items.length > 0 ? items[0] : ItemStack.EMPTY;
	}
	/** 展开期的一个待定节点：want=目标合成次数，shortfall=缺口个数，items=原料清单 */
	private record Draft(ItemStack target, Recipe<?> recipe, int outPer, int want, int shortfall, List<DraftItem> items) {}
	/** 一种原料：material 数量恒为 1，per=单次用量 */
	private record DraftItem(ItemStack material, int per) {}
}
