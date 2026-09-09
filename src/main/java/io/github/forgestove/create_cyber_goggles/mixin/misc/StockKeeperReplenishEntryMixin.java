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
 * <b>递归到原材料</b>：对每个「缺且可合成」的物品作合成节点（含配方+全部原料+可合成次数），
 * 其缺且可合成的原料并入 demand 继续拆，直到原材料。防环（一物品只拆一次）+ 限深。只读。
 */
@Mixin(value = StockKeeperRequestScreen.class, remap = false)
public abstract class StockKeeperReplenishEntryMixin extends AbstractSimiContainerScreen<StockKeeperRequestMenu>
	implements Self<StockKeeperRequestScreen> {
	/** 递归配方最大深度：防止「A↔B 互指」或过深链导致无限/崩溃 */
	@Unique private static final int MAX_RECURSION_DEPTH = 8;
	@Shadow List<List<ClipboardEntry>> clipboardItem;
	@Shadow StockTickerBlockEntity blockEntity;
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
			mc.setScreen(new AutoReplenishScreen(thiz(), blockEntity, groups));
		});
		btn.setToolTip(Component.translatable("create_cyber_goggles.gui.auto_replenish.title"));
		addRenderableWidget(btn);
	}
	@Unique
	private List<ReplenishGroup> ccg$buildGroups() {
		Map<String, List<Node>> byType = new LinkedHashMap<>();
		InventorySummary summary = blockEntity.getLastClientsideStockSnapshotAsSummary();
		if (summary == null || mc.level == null) return List.of();
		if (clipboardItem == null) {
			CCG.LOGGER.info("ccg autoReplenish: clipboardItem=null");
			return List.of();
		}
		Map<Item, Integer> demand = new LinkedHashMap<>();
		// 共享原料「可消耗」：每个节点按剩余库存算可合成次数，算完扣除消耗，后续节点看到剩余
		Map<Item, Integer> remaining = new HashMap<>();
		for (List<ClipboardEntry> page : clipboardItem)
			for (ClipboardEntry entry : page)
				if (!entry.icon.isEmpty() && entry.itemAmount > 0) demand.merge(entry.icon.getItem(), entry.itemAmount, Integer::sum);
		// BFS：每轮快照 demand 的 key 集合，处理所有未展开物品；递归把新原料 merge 回 demand，
		// 下一轮快照必然包含它们 → 不依赖任何队列顺序/contains，绝对不漏递归项。
		Set<String> done = new HashSet<>();
		Map<String, Integer> depth = new HashMap<>();
		boolean progressed;
		do {
			progressed = false;
			for (var itemId : new ArrayList<>(demand.keySet())) {
				String key = itemId.toString();
				if (!done.add(key)) continue;
				progressed = true;
				ItemStack item = itemId.getDefaultInstance();
				Integer needed = demand.get(itemId);
				if (needed == null || needed <= 0) continue;
				int haveItem = summary.getCountOf(item);
				CCG.LOGGER.info("  bfs node={} need={} have={}", item.getHoverName().getString(), needed, haveItem);
				if (haveItem >= needed) {
					CCG.LOGGER.info("    库存已够，跳过合成");
					continue;
				}
				Recipe<?> recipe = ccg$findAnyRecipeFor(item);
				if (recipe == null) {
					CCG.LOGGER.info("    无物品原料配方（原材料），跳过");
					continue;
				}
				CCG.LOGGER.info("    配方 type={}", recipe.getType());
				int output = Math.max(1, recipe.getResultItem(mc.level.registryAccess()).getCount());
				int wantTimes = (needed - haveItem + output - 1) / output;
				if (wantTimes <= 0) continue;
				// 每原料单次用量（每槽/每格消耗 1，同原料多槽累加）
				var per = new LinkedHashMap<ItemStack, Integer>();
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
				// 可合成次数 = min(目标次数, 各「原材料」floor(库存/per))；
				// 自己可合成的原料不限制本节点（它会被递归合成，其可用性由它自己的节点处理）
				int craftTimes = wantTimes;
				for (Entry<ItemStack, Integer> e : per.entrySet()) {
					int p = e.getValue();
					if (p <= 0) continue;
					if (ccg$isCraftable(e.getKey(), summary)) continue;
					int cur = remaining.getOrDefault(e.getKey().getItem(), summary.getCountOf(e.getKey()));
					craftTimes = Math.min(craftTimes, cur / p);
				}
				List<ReplenishEntry> items = new ArrayList<>();
				for (Entry<ItemStack, Integer> e : per.entrySet()) {
					ItemStack matId = e.getKey().copyWithCount(1);
					int size = remaining.getOrDefault(e.getKey().getItem(), summary.getCountOf(e.getKey()));
					items.add(new ReplenishEntry(matId, e.getValue(), size >= e.getValue(), ccg$isCraftable(matId, summary)));
				}
				CCG.LOGGER.info(
					"  node {} -> craftTimes={}/{} items={}",
					item.getHoverName().getString(),
					craftTimes,
					wantTimes,
					items.size()
				);
				byType.computeIfAbsent(recipe.getType().toString(), k -> new ArrayList<>())
					.add(new Node(item.copy(), wantTimes, recipe, craftTimes, items, depth.getOrDefault(key, 0)));
				// 消耗共享原料：本节点用了多少就从剩余扣掉，后续节点看到的是剩余（可消耗性）
				if (craftTimes > 0) for (ReplenishEntry e : items) {
					Item remKey = e.material().getItem();
					int cur = remaining.getOrDefault(remKey, summary.getCountOf(e.material()));
					int used = e.per() * craftTimes;
					int newRem = Math.max(0, cur - used);
					remaining.put(remKey, newRem);
					CCG.LOGGER.info("    消耗 {} {}→{} (使用{})", e.material().getHoverName().getString(), cur, newRem, used);
				}
				// 该配方要用的原料若可合成，继续拆；防环（一物品只拆一次）+ 限深
				for (ReplenishEntry e : items) {
					if (!e.craftable()) continue;
					String mkey = e.material().getItem().toString();
					if (done.contains(mkey)) {
						CCG.LOGGER.info("    防环/已处理: 不再拆 {}", e.material().getHoverName().getString());
						continue;
					}
					int myDepth = depth.getOrDefault(key, 0) + 1;
					if (myDepth > MAX_RECURSION_DEPTH) {
						CCG.LOGGER.info("    递归超深: 停止拆 {}", e.material().getHoverName().getString());
						continue;
					}
					Item matKey = e.material().getItem();
					// 按实际可合成次数(craftTimes)递归，避免为合不了的次数多订原料
					demand.merge(matKey, e.per() * craftTimes, Integer::sum);
					depth.put(mkey, myDepth);
					CCG.LOGGER.info(
						"    并入需求: {} +{} (现累计 {})",
						e.material().getHoverName().getString(),
						e.per() * craftTimes,
						demand.get(matKey)
					);
				}
			}
		} while (progressed);
		List<ReplenishGroup> out = new ArrayList<>();
		for (Entry<String, List<Node>> e : byType.entrySet())
			out.add(new ReplenishGroup(e.getKey(), e.getValue()));
		return out;
	}
	@Unique private final Map<Item, Boolean> ccg$craftableCache = new HashMap<>();
	/**
	 * 该物品能否「真正合成出来」：有配方，且配方每个原料都能被库存满足、或继续递归合成出来
	 * （而不是「存在任意配方」——那样 圆石→沙子 之类会让所有东西都判为可合成）。带缓存、防环、限深。
	 */
	@Unique
	private boolean ccg$isCraftable(ItemStack stack, InventorySummary summary) {
		var cached = ccg$craftableCache.get(stack.getItem());
		if (cached != null) return cached;
		var ok = ccg$canCraft(stack, summary, new HashSet<>(), 0);
		if (ok) ccg$craftableCache.put(stack.getItem(), true);   // 只缓存正向结果，避免环污染
		return ok;
	}
	@Unique
	private boolean ccg$canCraft(ItemStack target, InventorySummary summary, Set<Item> visiting, int depth) {
		if (depth > MAX_RECURSION_DEPTH) return false;
		if (!visiting.add(target.getItem())) return false;      // 环
		Recipe<?> recipe = ccg$findAnyRecipeFor(target);
		var ok = recipe != null;
		if (ok) for (Ingredient ing : recipe.getIngredients()) {
			if (ing.isEmpty()) continue;
			ItemStack pick = ccg$pickFromIngredient(ing, summary);
			if (pick.isEmpty()) {
				ok = false;
				break;
			}
			if (summary.getCountOf(pick) >= 1) continue;         // 库存有 → 该分支满足
			if (!ccg$canCraft(pick, summary, visiting, depth + 1)) {
				ok = false;
				break;
			}
		}
		visiting.remove(target.getItem());
		return ok;
	}
	@Unique
	private Recipe<?> ccg$findAnyRecipeFor(ItemStack target) {
		if (mc.level == null) return null;
		var registry = mc.level.registryAccess();
		Recipe<?> best = null;
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
			// 多个配方都能产出同一物品时取单次产出最多的（如 1 铁锭→9 铁粒 优于 1 铁锭→1 铁粒）
			if (result.getCount() > bestOut) {
				best = recipe;
				bestOut = result.getCount();
			}
		}
		if (best != null) CCG.LOGGER.info("    选中配方 {} 单次产出 {}x", best.getType(), bestOut);
		return best;
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
}
