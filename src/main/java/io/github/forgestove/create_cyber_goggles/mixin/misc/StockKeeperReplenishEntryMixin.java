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
import net.minecraft.resources.ResourceLocation;
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
 * ① 自顶向下展开依赖图（一物品一节点：选配方时避开会让依赖闭合的候选，「铁锭↔铁块」这类互逆
 * 只取一个方向、另一个当原材料）；② 自底向上算「产能上限」——父节点的可合成次数
 * 受<b>子节点实际产出</b>限制（不再把可合成原料当无限制）；③ 自顶向下按父的实际消耗分配需求，
 * 取 min(需求次数, 产能上限) 作为最终 craftTimes。共享原料按序消耗。只读。
 */
@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperReplenishEntryMixin extends AbstractSimiContainerScreen<StockKeeperRequestMenu>
	implements Self<StockKeeperRequestScreen>, CCGReplenishTree {
	/**
	 * 同物品多配方时的类型取舍顺序（越靠前越优先）。表内的类型 > 其他 Create 类型 > 非 Create 类型，
	 * 表里没列出的走默认档位，所以新增类型不会互相挤掉。
	 */
	@Unique private static final List<String> ccg$TYPE_ORDER = List.of(
		"create:crushing",        // 粉碎优于研磨
		"create:milling",
		"create:cutting",         // 切削优于原版切石
		"create:pressing",
		"create:compacting",
		"create:mixing",
		"create:splashing",
		"create:haunting",
		"create:deploying",       // 机械手部署优于手动物品应用
		"create:item_application"
	);
	/** 玩家手动指定的配方（物品 → 配方 ID），进程内有效；键为物品，同物品的所有节点一起换 */
	@Unique private static final Map<Item, ResourceLocation> ccg$recipeChoices = new HashMap<>();
	@Unique private final Map<Item, List<RecipeHolder<?>>> ccg$candidatesCache = new HashMap<>();
	@Shadow List<List<ClipboardEntry>> clipboardItem;
	@Shadow StockTickerBlockEntity blockEntity;
	@Unique private Set<Item> ccg$recipeOutputs;
	public StockKeeperReplenishEntryMixin(StockKeeperRequestMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	/**
	 * 该配方是否会让 target 依赖回自己：原料沿已展开的依赖图能走回 target 即闭合。
	 * 不在图里的原料必然走不回（{@link #ccg$reaches} 只沿图内的边），直接跳过。
	 */
	@Unique
	private static boolean ccg$createsCycle(Recipe<?> recipe, Item target, Map<Item, Draft> drafts) {
		if (drafts.isEmpty()) return false;
		for (Ingredient ing : recipe.getIngredients()) {
			if (ing.isEmpty()) continue;
			for (ItemStack option : ing.getItems())
				if (drafts.containsKey(option.getItem()) && ccg$reaches(drafts, option.getItem(), target, new HashSet<>())) return true;
		}
		return false;
	}
	/** 沿已展开的依赖图，from 能否走到 target（能走到说明 from→target 这条边会成环） */
	@Unique
	private static boolean ccg$reaches(Map<Item, Draft> drafts, Item from, Item target, Set<Item> visited) {
		if (from.equals(target)) return true;
		if (!visited.add(from)) return false;
		Draft d = drafts.get(from);
		if (d == null) return false;
		for (DraftItem di : d.items())
			if (ccg$reaches(drafts, di.material().getItem(), target, visited)) return true;
		return false;
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
	/** 原料当前可获得量：pool 里已有该物品就用剩余量（库存 + 已算出的产出 − 已分配），否则取库存 */
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
	@Inject(method = "init", at = @At("TAIL"))
	private void addAutoReplenishButton(CallbackInfo ci) {
		if (!CCG.config.misc.autoReplenishStock || !thiz().isSchematicListMode()) return;
		var btn = new IconButton(leftPos + imageWidth - 10, topPos + 20, AllIcons.I_ADD);
		btn.withCallback(() -> {
			List<ReplenishGroup> groups = ccg$buildGroups();
			var summary = blockEntity.getLastClientsideStockSnapshotAsSummary();
			CCG.LOGGER.debug("ccg autoReplenish click: groups={} summary={}", groups.size(), summary == null ? "null" : "ok");
			mc.setScreen(new AutoReplenishScreen(thiz(), blockEntity, groups));
		});
		btn.setToolTip(Component.translatable("create_cyber_goggles.gui.auto_replenish.title"));
		addRenderableWidget(btn);
	}
	/** {@link CCGReplenishTree}：界面切换配方后要重新构建整棵树 */
	@Unique
	@Override
	public List<ReplenishGroup> ccg$buildGroups() {
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
				Recipe<?> recipe = ccg$findAnyRecipeFor(item, summary, drafts);
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
				CCG.LOGGER.debug(
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
					if (!ccg$hasRecipe(di.material())) continue;
					// 兜底环检测：原料沿依赖图已经能回到本物品，再连这条边就成环（沙子↔砂岩、圆石↔安山岩↔闪长岩），
					// 环会让需求被反复放大（实测沙子涨到 1165 万）。正常情况配方选择（ccg$findAnyRecipeFor）
					// 已避开成环配方，这里只兜手动指定配方等漏网情形 → 断开该边，原料当原材料用库存
					if (ccg$reaches(drafts, matItem, itemId, new HashSet<>())) continue;
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
		//    「可获得量」= 仓库现有 + 该节点自己新合成出来的产出（所以初始值必须是库存，不能置 0，
		//    否则「仓库有 3 个 + 还需合成 1 个」这种半库存半合成的情况会被当成只有 1 个可用）。
		Map<Item, Integer> pool = new HashMap<>();
		for (Item itemId : drafts.keySet()) pool.put(itemId, summary.getCountOf(itemId.getDefaultInstance()));
		Map<Item, Integer> cap = new HashMap<>();
		// 每个节点处理时各原料的「拿到手时够不够一次配方」：界面上的勾选框与 blocked 判定必须同源，
		// 否则会出现「原料全是绿勾却躺在无法合成里」（仓库有货，但已经被排在前面的节点预留走了）
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
				int avail = ccg$avail(pool, di.material(), summary);
				enoughFlags.add(avail >= di.per());
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
			if (limited != null) CCG.LOGGER.debug(
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
		// 根需求里「找不到配方」（没有节点）却库存不足的物品：挂到无类型的兜底组（组名 lang 显示「无法合成」）
		for (Entry<Item, Integer> e : rootNeed.entrySet()) {
			if (drafts.containsKey(e.getKey())) continue;
			int have = summary.getCountOf(e.getKey().getDefaultInstance());
			if (have >= e.getValue()) continue;
			missingByType.computeIfAbsent("", k -> new LinkedHashMap<>()).merge(e.getKey(), e.getValue() - have, Integer::sum);
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
		// 6) 按配方类型组装：能合成的进 nodes，缺料合不出来的进 blocked（界面单独一行「无法合成的配方」）；
		//    缺失原料和 blocked 都留在各自的组里，不再全局合并
		Map<String, List<Node>> byType = new LinkedHashMap<>();
		Map<String, List<Node>> blockedByType = new LinkedHashMap<>();
		for (Draft d : drafts.values()) {
			Item itemId = d.target().getItem();
			int t = times.getOrDefault(itemId, 0);
			String type = d.recipe().getType().toString();
			// enough 用步骤 4 里「拿到手时的可用量」判定，保证和 blocked 同源
			List<Boolean> flags = enoughByNode.getOrDefault(itemId, List.of());
			List<ReplenishEntry> entries = new ArrayList<>(d.items().size());
			for (var i = 0; i < d.items().size(); i++) {
				DraftItem di = d.items().get(i);
				var enough = i < flags.size() && flags.get(i);
				entries.add(new ReplenishEntry(di.material(), di.per(), enough, drafts.containsKey(di.material().getItem())));
			}
			int nodeDepth = depth.getOrDefault(itemId, 0);
			var node = new Node(d.target().copy(), d.want(), d.recipe(), t, entries, nodeDepth, ccg$candidateCards(d.target(), summary));
			if (t <= 0) {
				blockedByType.computeIfAbsent(type, k -> new ArrayList<>()).add(node);
				CCG.LOGGER.debug("ccg autoReplenish blocked: {}", d.target().getHoverName().getString());
				continue;
			}
			byType.computeIfAbsent(type, k -> new ArrayList<>()).add(node);
			CCG.LOGGER.debug(
				"ccg autoReplenish node: {} craft={}/{} depth={}",
				d.target().getHoverName().getString(),
				t,
				d.want(),
				nodeDepth
			);
		}
		// 有缺失或 blocked 但没节点的类型也要成组（组名 = 配方类型；空串是「无法合成」兜底组）
		for (String type : missingByType.keySet()) byType.computeIfAbsent(type, k -> new ArrayList<>());
		for (String type : blockedByType.keySet()) byType.computeIfAbsent(type, k -> new ArrayList<>());
		List<ReplenishGroup> out = new ArrayList<>();
		for (String type : byType.keySet()) {
			Map<Item, Integer> m = missingByType.getOrDefault(type, Map.of());
			List<ItemStack> missingList = new ArrayList<>(m.size());
			m.forEach((item, count) -> missingList.add(new ItemStack(item, count)));
			out.add(new ReplenishGroup(type, byType.get(type), blockedByType.getOrDefault(type, List.of()), missingList));
		}
		missingByType.entrySet().removeIf(e -> e.getValue().isEmpty());   // 日志只留真正有缺口的组
		if (!missingByType.isEmpty()) CCG.LOGGER.debug("ccg autoReplenish missing: {}", missingByType);
		return out;
	}
	/** 该物品的候选配方（带缓存）：{@link #ccg$findAnyRecipeFor} 与界面弹窗共用同一份列表 */
	@Unique
	private List<RecipeHolder<?>> ccg$candidates(ItemStack target, InventorySummary summary) {
		return ccg$candidatesCache.computeIfAbsent(target.getItem(), k -> ccg$candidatesFor(target, summary));
	}
	/** 候选配方 + 各自单次的原料清单（界面弹窗里悬浮某行时按配方卡样式显示） */
	@Unique
	private List<CandidateCard> ccg$candidateCards(ItemStack target, InventorySummary summary) {
		List<RecipeHolder<?>> holders = ccg$candidates(target, summary);
		List<CandidateCard> out = new ArrayList<>(holders.size());
		for (RecipeHolder<?> holder : holders) out.add(new CandidateCard(holder, ccg$entriesFor(holder.value(), summary)));
		return out;
	}
	/**
	 * 某个配方单次的原料清单：{@code enough} 只能按「仓库库存 ≥ 单次用量」判——候选配方没参与主求解，
	 * 拿不到「拿到手时的可用量」；{@code craftable} 同节点，用产物表判断。
	 */
	@Unique
	private List<ReplenishEntry> ccg$entriesFor(Recipe<?> recipe, InventorySummary summary) {
		List<DraftItem> items = ccg$draftItems(recipe, summary);
		List<ReplenishEntry> out = new ArrayList<>(items.size());
		for (DraftItem di : items)
			out.add(new ReplenishEntry(
				di.material(),
				di.per(),
				summary.getCountOf(di.material()) >= di.per(),
				ccg$hasRecipe(di.material())
			));
		return out;
	}
	/** 该物品的全部候选配方，已按优先级排好序（界面点节点也用这个列表让玩家切换） */
	@Unique
	public List<RecipeHolder<?>> ccg$candidatesFor(ItemStack target, InventorySummary summary) {
		if (mc.level == null) return List.of();
		var registry = mc.level.registryAccess();
		List<Candidate> found = new ArrayList<>();
		outer:
		for (RecipeHolder<?> holder : mc.level.getRecipeManager().getRecipes()) {
			Recipe<?> recipe = holder.value();
			if (recipe.getIngredients().isEmpty()) continue;
			// 产物按 Item 比即可（忽略组件）：Create 的加工配方产物常带组件，用
			// isSameItemSameComponents 会把「同一物品的其他类型配方」整批漏掉，候选列表里只剩同类
			ItemStack result = recipe.getResultItem(registry);
			if (result.isEmpty() || result.getItem() != target.getItem()) continue;
			// 产物不能同时是自己的原料（自循环配方），跳过
			for (Ingredient ing : recipe.getIngredients())
				if (!ing.isEmpty() && ing.test(target)) {
					CCG.LOGGER.debug("    跳过自循环配方 {} (产物 {} 也是原料)", recipe.getType(), target.getHoverName().getString());
					continue outer;
				}
			// 排序优先级（从高到低）：
			// ① 原料现在仓库里就有（每个 Ingredient 都能挑到有库存的物品）——比「看起来更基础」重要：
			//    仓库有铁块时「1 铁块→9 铁锭」应压过「铁马铠粉碎→1 铁锭」；
			// ② 配方类型的取舍顺序 ccg$TYPE_ORDER（数值越小越优先）：切削 > 原版切石、
			//    粉碎 > 研磨、机械手部署 > 手动物品应用；表外的 Create 类型次之，非 Create 最后；
			// ③ 单次产出最多（效率，如 9 铁锭 优于 1 铁锭、1 铁锭→9 铁粒 优于 1 铁粒）；
			// ④ 原料更「基础」（原料里没有配方的越多越基础），避免选中「1 铁块→9 铁锭」这类
			//    逆向配方与「9 铁锭→1 铁块」互锁 → 整条链 cap=0
			var stocked = true;
			var raw = 0;
			for (Ingredient ing : recipe.getIngredients()) {
				if (ing.isEmpty()) continue;
				var hasStock = false;
				var craftable = false;
				for (ItemStack option : ing.getItems()) {
					if (!hasStock && summary.getCountOf(option) > 0) hasStock = true;
					if (!craftable && ccg$hasRecipe(option)) craftable = true;
					if (hasStock && craftable) break;
				}
				if (!hasStock) stocked = false;
				if (!craftable) raw++;
			}
			var typeId = recipe.getType().toString();
			var rank = ccg$TYPE_ORDER.indexOf(typeId);
			if (rank < 0) rank = typeId.startsWith("create:") ? ccg$TYPE_ORDER.size() : ccg$TYPE_ORDER.size() + 1;
			found.add(new Candidate(stocked, rank, result.getCount(), raw, holder));
		}
		found.sort(Comparator.comparing((Candidate c) -> !c.stocked())
			.thenComparingInt(Candidate::rank)
			.thenComparing(c -> -c.out())
			.thenComparing(c -> -c.raw()));
		List<RecipeHolder<?>> out = new ArrayList<>(found.size());
		found.forEach(c -> out.add(c.holder()));
		if (!out.isEmpty()) CCG.LOGGER.debug("    候选配方 {} 个，首选 {}", out.size(), out.getFirst().value().getType());
		return out;
	}
	/**
	 * 选中的配方：玩家手动指定优先；否则按候选顺序取<b>第一个不引入环</b>的
	 * （如已展开「铁块 ← 9 铁锭」，铁锭再走「1 铁块 → 9 铁锭」就闭合，改选熔炼等不成环的候选）。
	 * 全部候选都会成环 → 返回 null，该物品按<b>原材料</b>处理（只用库存、不进依赖图），
	 * 这样「铁锭↔铁块」这类互逆配方只会取一个方向，另一个当缺料显示，不会两边互相发订单抵消。
	 */
	@Unique
	private Recipe<?> ccg$findAnyRecipeFor(ItemStack target, InventorySummary summary, Map<Item, Draft> drafts) {
		if (mc.level == null) return null;
		Item key = target.getItem();
		var chosen = ccg$recipeChoices.get(key);
		if (chosen != null) {
			var forced = mc.level.getRecipeManager().byKey(chosen).map(RecipeHolder::value).orElse(null);
			if (forced != null) return forced;   // 手动指定：尊重玩家选择，不避环（由断环兜底）
		}
		List<RecipeHolder<?>> candidates = ccg$candidates(target, summary);
		for (RecipeHolder<?> holder : candidates)
			if (!ccg$createsCycle(holder.value(), key, drafts)) {
				CCG.LOGGER.debug("    选中配方 {} (候选 {} 个)", holder.value().getType(), candidates.size());
				return holder.value();
			}
		if (!candidates.isEmpty())
			CCG.LOGGER.debug("    候选配方全部成环，{} 按原材料处理 (候选 {} 个)", target.getHoverName().getString(), candidates.size());
		return null;
	}
	/** 该物品指定用哪个配方（null = 恢复自动选优）。选择不缓存，下次构建即生效 */
	@Unique
	@Override
	public void ccg$setRecipeChoice(Item item, ResourceLocation recipeId) {
		if (recipeId == null) ccg$recipeChoices.remove(item);
		else ccg$recipeChoices.put(item, recipeId);
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
	/** 候选配方的排序键 */
	private record Candidate(boolean stocked, int rank, int out, int raw, RecipeHolder<?> holder) {}
	/** 展开期的一个待定节点：want=目标合成次数，shortfall=缺口个数，items=原料清单 */
	private record Draft(ItemStack target, Recipe<?> recipe, int outPer, int want, int shortfall, List<DraftItem> items) {}
	/** 一种原料：material 数量恒为 1，per=单次用量 */
	private record DraftItem(ItemStack material, int per) {}
}
