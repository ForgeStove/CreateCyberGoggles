package io.github.forgestove.create_cyber_goggles.core.factory;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import io.github.forgestove.create_cyber_goggles.core.factory.ReplenishGroup.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import org.slf4j.Logger;

import java.util.*;
import java.util.Map.Entry;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
/**
 * 「自动补齐缺货」的配方树求解核心，与宿主界面无关：输入剪贴板蓝图清单 + 仓库库存快照，
 * 输出按配方类型分组的 {@link ReplenishGroup} 列表。递归到原材料，三阶段求解（自顶向下展开依赖图 →
 * 自底向上算产能上限 → 自顶向下按父的实际消耗分配需求），保证发出的配方原料一定齐备。
 */
public final class ReplenishPlanner {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final List<String> TYPE_ORDER = List.of(
		"create:crushing",
		"create:milling",
		"create:cutting",
		"create:pressing",
		"create:compacting",
		"create:mixing",
		"create:splashing",
		"create:haunting",
		"create:deploying",
		"create:item_application"
	);
	/** 玩家手动指定的配方，进程内有效；键为物品，同物品的所有节点一起换。 */
	private static final Map<Item, ResourceLocation> RECIPE_CHOICES = new HashMap<>();
	/** 候选配方与产物表缓存，绑定到 RecipeManager，换存档或数据包重载后重建。 */
	private static Object cacheOwner;
	private static Map<Item, List<RecipeHolder<?>>> candidatesCache = new HashMap<>();
	private static Set<Item> recipeOutputs;
	/** 主手优先、其次副手读取剪贴板蓝图清单；整页没有任何物品条目则视为没有清单。 */
	public static List<List<ClipboardEntry>> clipboardOf(Player player) {
		if (player == null) return null;
		for (var hand : new InteractionHand[]{InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND}) {
			ItemStack stack = player.getItemInHand(hand);
			if (!AllBlocks.CLIPBOARD.isIn(stack)) continue;
			List<List<ClipboardEntry>> content = ClipboardEntry.readAll(stack);
			for (List<ClipboardEntry> page : content)
				for (ClipboardEntry entry : page)
					if (!entry.icon.isEmpty()) return content;
		}
		return null;
	}
	/** 把剪贴板清单里仓库现有的物品按 {@code min(清单数量, 库存)} 铺成订单，同物品跨页合并。 */
	public static List<BigItemStack> schematicOrders(List<List<ClipboardEntry>> clipboard, InventorySummary summary) {
		if (clipboard == null || summary == null) return List.of();
		Map<Item, Integer> want = new LinkedHashMap<>();
		for (List<ClipboardEntry> page : clipboard)
			for (ClipboardEntry entry : page)
				if (!entry.icon.isEmpty() && entry.itemAmount > 0) want.merge(entry.icon.getItem(), entry.itemAmount, Integer::sum);
		List<BigItemStack> out = new ArrayList<>(want.size());
		for (Entry<Item, Integer> entry : want.entrySet()) {
			Item item = entry.getKey();
			Integer amount = entry.getValue();
			int have = summary.getCountOf(item.getDefaultInstance());
			if (have <= 0) continue;
			out.add(new BigItemStack(item.getDefaultInstance(), Math.min(amount, have)));
		}
		return out;
	}
	/** 指定该物品用哪个配方，null 恢复自动选优；不缓存，下次构建生效。 */
	public static void setRecipeChoice(Item item, ResourceLocation recipeId) {
		if (recipeId == null) RECIPE_CHOICES.remove(item);
		else RECIPE_CHOICES.put(item, recipeId);
	}
	/** 构建整棵依赖树；入参为空或没有可补条目时返回空列表。 */
	public static List<ReplenishGroup> build(List<List<ClipboardEntry>> clipboard, InventorySummary summary) {
		if (summary == null || mc.level == null || clipboard == null) return List.of();
		ensureCacheOwner();
		// 裸 Item 键：ItemStack 的 equals 含 count 与组件，同物品会被拆成多个键
		Map<Item, Integer> rootNeed = new LinkedHashMap<>();
		for (List<ClipboardEntry> page : clipboard)
			for (ClipboardEntry entry : page)
				if (!entry.icon.isEmpty() && entry.itemAmount > 0) rootNeed.merge(entry.icon.getItem(), entry.itemAmount, Integer::sum);
		if (rootNeed.isEmpty()) return List.of();
		// 自顶向下展开依赖图：一物品一节点，缺多少记在 shortfall；需求会被多个父节点追加，故按「上次传播的 want」差量补发，环用轮次上限兜底
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
				if (have >= needed) continue;
				Recipe<?> recipe = findAnyRecipeFor(item, summary, drafts);
				if (recipe == null) continue;
				List<DraftItem> items = draftItems(recipe, summary);
				if (items.isEmpty()) continue;
				int outPer = Math.max(1, recipe.getResultItem(mc.level.registryAccess()).getCount());
				int want = (needed - have + outPer - 1) / outPer;
				Integer prev = spreadWant.get(itemId);
				if (prev != null && prev == want) continue;
				drafts.put(itemId, new Draft(item, recipe, outPer, want, needed - have, items));
				spreadWant.put(itemId, want);
				progressed = true;
				LOGGER.debug(
					"AutoReplenish expand: {} need={} have={} → want={} (单次产出 {})",
					item.getHoverName().getString(),
					needed,
					have,
					want,
					outPer
				);

				int delta = want - (prev == null ? 0 : prev);
				for (DraftItem di : items) {
					Item matItem = di.material().getItem();
					if (!hasRecipe(di.material())) continue;
					// 兜底环检测：成环会让需求被反复放大，故断开该边；正常选配方已避环，这里只兜手动指定等漏网情形
					if (reaches(drafts, matItem, itemId, new HashSet<>())) continue;
					pending.merge(matItem, di.per() * delta, Integer::sum);
				}
			}
			if (!progressed) break;
		}
		// drafts 为空也要继续：没有配方的物品必须出现在缺失列表里
		// 最长路径深度，保证父 < 子；深度倒序即「子先父后」的拓扑序
		Map<Item, Integer> depth = depths(drafts, rootNeed.keySet());
		List<Item> deepFirst = new ArrayList<>(drafts.keySet());
		deepFirst.sort(Comparator.comparingInt((Item i) -> depth.getOrDefault(i, 0)).reversed());
		// 产能上限 cap 取 min(缺口需求, 原料可获得量)，共享原料按序消耗；可获得量含仓库现有 + 自身新合成产出，故初始值取库存而非 0
		Map<Item, Integer> pool = new HashMap<>();
		for (Item itemId : drafts.keySet()) pool.put(itemId, summary.getCountOf(itemId.getDefaultInstance()));
		Map<Item, Integer> cap = new HashMap<>();
		// 各原料「拿到手时够不够一次配方」：勾选框与 blocked 判定必须同源，否则会出现原料全绿勾却躺在无法合成里
		Map<Item, List<Boolean>> enoughByNode = new HashMap<>();
		Map<String, Map<Item, Integer>> missingByType = new LinkedHashMap<>();
		for (Item itemId : deepFirst) {
			Draft d = drafts.get(itemId);
			int times = d.want();
			Item limited = null;
			var limitedAvail = 0;
			var limitedPer = 0;
			Map<Item, Integer> missing = missingByType.computeIfAbsent(d.recipe().getType().toString(), k -> new LinkedHashMap<>());
			List<Boolean> enoughFlags = new ArrayList<>(d.items().size());
			enoughByNode.put(itemId, enoughFlags);
			for (DraftItem di : d.items()) {
				if (di.per() <= 0) {
					enoughFlags.add(true);
					continue;
				}
				int avail = avail(pool, di.material(), summary);
				enoughFlags.add(avail >= di.per());
				if (avail / di.per() < times) {
					times = avail / di.per();
					limited = di.material().getItem();
					limitedAvail = avail;
					limitedPer = di.per();
				}
				// 不可合成且扣掉前面节点消耗后仍不够 → 记缺口
				int need = di.per() * d.want();
				if (!drafts.containsKey(di.material().getItem()) && avail < need)
					missing.merge(di.material().getItem(), need - avail, Integer::sum);
			}
			times = Math.max(0, times);
			if (limited != null) LOGGER.debug(
				"AutoReplenish cap: {} 受限于 {} (可用 {} / 每次 {}) → cap={}",
				d.target().getHoverName().getString(),
				limited.getDefaultInstance().getHoverName().getString(),
				limitedAvail,
				limitedPer,
				times
			);
			for (DraftItem di : d.items()) consume(pool, di.material(), di.per() * times);
			pool.merge(itemId, times * d.outPer(), Integer::sum);
			cap.put(itemId, times);
		}
		// 找不到配方却库存不足的根需求物品挂到兜底组（类型空串）
		for (Entry<Item, Integer> e : rootNeed.entrySet()) {
			if (drafts.containsKey(e.getKey())) continue;
			int have = summary.getCountOf(e.getKey().getDefaultInstance());
			if (have >= e.getValue()) continue;
			missingByType.computeIfAbsent("", k -> new LinkedHashMap<>()).merge(e.getKey(), e.getValue() - have, Integer::sum);
		}
		// 实际合成次数：需求由父节点实际消耗累加，且不超过产能上限
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
		// 按配方类型组装：能合成的进 nodes，缺料合不出来的进 blocked；缺失原料留在各自的组里
		Map<String, List<Node>> byType = new LinkedHashMap<>();
		Map<String, List<Node>> blockedByType = new LinkedHashMap<>();
		for (Draft d : drafts.values()) {
			Item itemId = d.target().getItem();
			int t = times.getOrDefault(itemId, 0);
			String type = d.recipe().getType().toString();
			// enough 用拿到手时的可用量判定，与 blocked 同源
			List<Boolean> flags = enoughByNode.getOrDefault(itemId, List.of());
			List<ReplenishEntry> entries = new ArrayList<>(d.items().size());
			for (var i = 0; i < d.items().size(); i++) {
				DraftItem di = d.items().get(i);
				var enough = i < flags.size() && flags.get(i);
				entries.add(new ReplenishEntry(di.material(), di.per(), enough, drafts.containsKey(di.material().getItem())));
			}
			int nodeDepth = depth.getOrDefault(itemId, 0);
			var node = new Node(d.target().copy(), d.want(), d.recipe(), t, entries, nodeDepth, candidateCards(d.target(), summary));
			if (t <= 0) {
				blockedByType.computeIfAbsent(type, k -> new ArrayList<>()).add(node);
				LOGGER.debug("Blocked: {}", d.target().getHoverName().getString());
				continue;
			}
			byType.computeIfAbsent(type, k -> new ArrayList<>()).add(node);
			LOGGER.debug("Node: {} craft={}/{} depth={}", d.target().getHoverName().getString(), t, d.want(), nodeDepth);
		}
		// 有缺失或 blocked 但没节点的类型也要成组
		for (String type : missingByType.keySet()) byType.computeIfAbsent(type, k -> new ArrayList<>());
		for (String type : blockedByType.keySet()) byType.computeIfAbsent(type, k -> new ArrayList<>());
		List<ReplenishGroup> out = new ArrayList<>();
		for (String type : byType.keySet()) {
			Map<Item, Integer> m = missingByType.getOrDefault(type, Map.of());
			List<ItemStack> missingList = new ArrayList<>(m.size());
			m.forEach((item, count) -> missingList.add(new ItemStack(item, count)));
			out.add(new ReplenishGroup(type, byType.get(type), blockedByType.getOrDefault(type, List.of()), missingList));
		}
		missingByType.entrySet().removeIf(e -> e.getValue().isEmpty());
		if (!missingByType.isEmpty()) LOGGER.debug("AutoReplenish missing: {}", missingByType);
		return out;
	}
	/** 换存档或重载数据包后 RecipeManager 会变，候选与产物表必须重建。 */
	private static void ensureCacheOwner() {
		var manager = mc.level == null ? null : mc.level.getRecipeManager();
		if (manager == cacheOwner) return;
		cacheOwner = manager;
		candidatesCache = new HashMap<>();
		recipeOutputs = null;
	}
	/** 该配方是否会让 target 依赖回自己：原料沿已展开的依赖图能走回 target 即闭合。 */
	private static boolean createsCycle(Recipe<?> recipe, Item target, Map<Item, Draft> drafts) {
		if (drafts.isEmpty()) return false;
		for (Ingredient ing : recipe.getIngredients()) {
			if (ing.isEmpty()) continue;
			for (ItemStack option : ing.getItems())
				if (drafts.containsKey(option.getItem()) && reaches(drafts, option.getItem(), target, new HashSet<>())) return true;
		}
		return false;
	}
	/** 沿已展开的依赖图判断 from 能否走到 target。 */
	private static boolean reaches(Map<Item, Draft> drafts, Item from, Item target, Set<Item> visited) {
		if (from.equals(target)) return true;
		if (!visited.add(from)) return false;
		Draft d = drafts.get(from);
		if (d == null) return false;
		for (DraftItem di : d.items())
			if (reaches(drafts, di.material().getItem(), target, visited)) return true;
		return false;
	}
	/** 单个配方的原料清单，同物品多槽累加 per。 */
	private static List<DraftItem> draftItems(Recipe<?> recipe, InventorySummary summary) {
		Map<ItemStack, Integer> per = new LinkedHashMap<>();
		for (Ingredient ing : recipe.getIngredients()) {
			if (ing.isEmpty()) continue;
			ItemStack pick = pickFromIngredient(ing, summary);
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
	/** 最长路径深度（父恒小于子）；环会让深度持续增长，用轮次上限兜底。 */
	private static Map<Item, Integer> depths(Map<Item, Draft> drafts, Set<Item> roots) {
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
	/** 原料当前可获得量：pool 有则取剩余量，否则取库存。 */
	private static int avail(Map<Item, Integer> pool, ItemStack material, InventorySummary summary) {
		return pool.computeIfAbsent(material.getItem(), k -> summary.getCountOf(k.getDefaultInstance()));
	}
	/** 扣减已消耗的原料；调用前须先经 {@link #avail} 初始化，否则会 merge 出负值。 */
	private static void consume(Map<Item, Integer> pool, ItemStack material, int amount) {
		if (amount <= 0) return;
		pool.merge(material.getItem(), -amount, Integer::sum);
	}
	private static ItemStack pickFromIngredient(Ingredient ing, InventorySummary summary) {
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
	/** 该物品的候选配方，带缓存；{@link #findAnyRecipeFor} 与界面弹窗共用。 */
	private static List<RecipeHolder<?>> candidates(ItemStack target, InventorySummary summary) {
		return candidatesCache.computeIfAbsent(target.getItem(), k -> candidatesFor(target, summary));
	}
	/** 候选配方 + 各自单次的原料清单。 */
	private static List<CandidateCard> candidateCards(ItemStack target, InventorySummary summary) {
		List<RecipeHolder<?>> holders = candidates(target, summary);
		List<CandidateCard> out = new ArrayList<>(holders.size());
		for (RecipeHolder<?> holder : holders) out.add(new CandidateCard(holder, entriesFor(holder.value(), summary)));
		return out;
	}
	/** 候选配方单次的原料清单：{@code enough} 只能按「仓库库存 ≥ 单次用量」判，候选配方没参与主求解、拿不到拿到手时的可用量。 */
	private static List<ReplenishEntry> entriesFor(Recipe<?> recipe, InventorySummary summary) {
		List<DraftItem> items = draftItems(recipe, summary);
		List<ReplenishEntry> out = new ArrayList<>(items.size());
		for (DraftItem di : items)
			out.add(new ReplenishEntry(di.material(), di.per(), summary.getCountOf(di.material()) >= di.per(), hasRecipe(di.material())));
		return out;
	}
	/** 该物品的全部候选配方，已按优先级排序。 */
	public static List<RecipeHolder<?>> candidatesFor(ItemStack target, InventorySummary summary) {
		if (mc.level == null) return List.of();
		var registry = mc.level.registryAccess();
		List<Candidate> found = new ArrayList<>();
		outer:
		for (RecipeHolder<?> holder : mc.level.getRecipeManager().getRecipes()) {
			Recipe<?> recipe = holder.value();
			if (recipe.getIngredients().isEmpty()) continue;
			// 产物按 Item 比即可：Create 的加工配方产物常带组件，用 isSameItemSameComponents 会把同物品的其他类型配方整批漏掉
			ItemStack result = recipe.getResultItem(registry);
			if (result.isEmpty() || result.getItem() != target.getItem()) continue;
			for (Ingredient ing : recipe.getIngredients())
				if (!ing.isEmpty() && ing.test(target)) {
					LOGGER.debug("跳过自循环配方 {} (产物 {} 也是原料)", recipe.getType(), target.getHoverName().getString());
					continue outer;
				}
			// 排序：原料有库存 > 配方类型优先级(TYPE_ORDER) > 单次产出多 > 原料更基础（避免选中互锁的逆向配方）
			var stocked = true;
			var raw = 0;
			for (Ingredient ing : recipe.getIngredients()) {
				if (ing.isEmpty()) continue;
				var hasStock = false;
				var craftable = false;
				for (ItemStack option : ing.getItems()) {
					if (!hasStock && summary.getCountOf(option) > 0) hasStock = true;
					if (!craftable && hasRecipe(option)) craftable = true;
					if (hasStock && craftable) break;
				}
				if (!hasStock) stocked = false;
				if (!craftable) raw++;
			}
			var typeId = recipe.getType().toString();
			var rank = TYPE_ORDER.indexOf(typeId);
			if (rank < 0) rank = typeId.startsWith("create:") ? TYPE_ORDER.size() : TYPE_ORDER.size() + 1;
			found.add(new Candidate(stocked, rank, result.getCount(), raw, holder));
		}
		found.sort(Comparator.comparing((Candidate c) -> !c.stocked())
			.thenComparingInt(Candidate::rank)
			.thenComparing(c -> -c.out())
			.thenComparing(c -> -c.raw()));
		List<RecipeHolder<?>> out = new ArrayList<>(found.size());
		found.forEach(c -> out.add(c.holder()));
		if (!out.isEmpty()) LOGGER.debug("候选配方 {} 个，首选 {}", out.size(), out.getFirst().value().getType());
		return out;
	}
	/** 玩家手动指定优先，否则取第一个不引入环的候选；全部成环时返回 null，该物品按原材料处理（只用库存、不进依赖图）。 */
	private static Recipe<?> findAnyRecipeFor(ItemStack target, InventorySummary summary, Map<Item, Draft> drafts) {
		if (mc.level == null) return null;
		Item key = target.getItem();
		var chosen = RECIPE_CHOICES.get(key);
		if (chosen != null) {
			var forced = mc.level.getRecipeManager().byKey(chosen).map(RecipeHolder::value).orElse(null);
			if (forced != null) return forced; // 手动指定不避环，由断环兜底
		}
		List<RecipeHolder<?>> candidates = candidates(target, summary);
		for (RecipeHolder<?> holder : candidates)
			if (!createsCycle(holder.value(), key, drafts)) {
				LOGGER.debug("选中配方 {} (候选 {} 个)", holder.value().getType(), candidates.size());
				return holder.value();
			}
		if (!candidates.isEmpty())
			LOGGER.debug("候选配方全部成环，{} 按原材料处理 (候选 {} 个)", target.getHoverName().getString(), candidates.size());
		return null;
	}
	/** 该物品是否是某个配方的产物；只查产物表、不递归。 */
	private static boolean hasRecipe(ItemStack target) {
		if (recipeOutputs == null) {
			recipeOutputs = new HashSet<>();
			if (mc.level != null) for (RecipeHolder<?> holder : mc.level.getRecipeManager().getRecipes()) {
				Recipe<?> recipe = holder.value();
				if (recipe.getIngredients().isEmpty()) continue;
				ItemStack out = recipe.getResultItem(mc.level.registryAccess());
				if (!out.isEmpty()) recipeOutputs.add(out.getItem());
			}
		}
		return recipeOutputs.contains(target.getItem());
	}
	private record Candidate(boolean stocked, int rank, int out, int raw, RecipeHolder<?> holder) {}
	/** 展开期的待定节点；want=目标合成次数，shortfall=缺口个数。 */
	private record Draft(ItemStack target, Recipe<?> recipe, int outPer, int want, int shortfall, List<DraftItem> items) {}
	/** 一种原料；material 数量恒为 1，per=单次用量。 */
	private record DraftItem(ItemStack material, int per) {}
}
