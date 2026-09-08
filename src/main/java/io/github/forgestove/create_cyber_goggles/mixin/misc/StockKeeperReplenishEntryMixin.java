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
				// 可合成次数 = min(目标次数, 各原料 floor(库存/per))；某原料连一次都不够 → 0（阻止本组）
				int craftTimes = wantTimes;
				for (Entry<ItemStack, Integer> e : per.entrySet()) {
					int p = e.getValue();
					if (p <= 0) continue;
					int cur = remaining.getOrDefault(e.getKey().getItem(), summary.getCountOf(e.getKey()));
					craftTimes = Math.min(craftTimes, cur / p);
				}
				List<ReplenishEntry> items = new ArrayList<>();
				for (Entry<ItemStack, Integer> e : per.entrySet()) {
					ItemStack matId = e.getKey().copyWithCount(1);
					int size = remaining.getOrDefault(e.getKey().getItem(), summary.getCountOf(e.getKey()));
					items.add(new ReplenishEntry(matId, e.getValue(), size >= e.getValue()));
				}
				CCG.LOGGER.info(
					"  node {} -> craftTimes={}/{} items={}",
					item.getHoverName().getString(),
					craftTimes,
					wantTimes,
					items.size()
				);
				byType.computeIfAbsent(recipe.getType().toString(), k -> new ArrayList<>())
					.add(new Node(item.copy(), wantTimes, recipe, craftTimes, items));
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
					if (ccg$findAnyRecipeFor(e.material()) == null) continue;
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
					demand.merge(matKey, e.per() * wantTimes, Integer::sum);
					depth.put(mkey, myDepth);
					CCG.LOGGER.info(
						"    并入需求: {} +{} (现累计 {})",
						e.material().getHoverName().getString(),
						e.per() * wantTimes,
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
	@Unique
	private Recipe<?> ccg$findAnyRecipeFor(ItemStack target) {
		if (mc.level == null) return null;
		var registry = mc.level.registryAccess();
		for (RecipeHolder<?> holder : mc.level.getRecipeManager().getRecipes()) {
			Recipe<?> recipe = holder.value();
			if (recipe.getIngredients().isEmpty()) continue;
			ItemStack result = recipe.getResultItem(registry);
			if (!result.isEmpty() && ItemStack.isSameItemSameComponents(result, target)) return recipe;
		}
		return null;
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
