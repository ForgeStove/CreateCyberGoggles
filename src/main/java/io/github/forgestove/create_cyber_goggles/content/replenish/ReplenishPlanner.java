package io.github.forgestove.create_cyber_goggles.content.replenish;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllBlocks;
import io.github.forgestove.create_cyber_goggles.CCG;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import io.github.forgestove.create_cyber_goggles.content.replenish.ReplenishGroup.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import org.slf4j.Logger;

import java.util.*;
import java.util.Map.Entry;

import static io.github.forgestove.create_cyber_goggles.foundation.util.CCGUtil.mc;
public class ReplenishPlanner {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<Item, ResourceLocation> RECIPE_CHOICES = new HashMap<>();
	private static Object cacheOwner;
	private static Map<Item, List<RecipeHolder<?>>> candidatesCache = new HashMap<>();
	private static Set<Item> recipeOutputs;
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
	public static void setRecipeChoice(Item item, ResourceLocation recipeId) {
		if (recipeId == null) RECIPE_CHOICES.remove(item);
		else RECIPE_CHOICES.put(item, recipeId);
	}
	public static List<ReplenishGroup> build(List<List<ClipboardEntry>> clipboard, InventorySummary summary) {
		if (summary == null || mc.level == null || clipboard == null) return List.of();
		ensureCacheOwner();
		Map<Item, Integer> rootNeed = new LinkedHashMap<>();
		for (List<ClipboardEntry> page : clipboard)
			for (ClipboardEntry entry : page)
				if (!entry.icon.isEmpty() && entry.itemAmount > 0) rootNeed.merge(entry.icon.getItem(), entry.itemAmount, Integer::sum);
		if (rootNeed.isEmpty()) return List.of();
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
					if (reaches(drafts, matItem, itemId, new HashSet<>())) continue;
					pending.merge(matItem, di.per() * delta, Integer::sum);
				}
			}
			if (!progressed) break;
		}
		Map<Item, Integer> depth = depths(drafts, rootNeed.keySet());
		List<Item> deepFirst = new ArrayList<>(drafts.keySet());
		deepFirst.sort(Comparator.comparingInt((Item i) -> depth.getOrDefault(i, 0)).reversed());
		Map<Item, Integer> pool = new HashMap<>();
		for (Item itemId : drafts.keySet()) pool.put(itemId, summary.getCountOf(itemId.getDefaultInstance()));
		Map<Item, Integer> cap = new HashMap<>();
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
		for (Entry<Item, Integer> e : rootNeed.entrySet()) {
			if (drafts.containsKey(e.getKey())) continue;
			int have = summary.getCountOf(e.getKey().getDefaultInstance());
			if (have >= e.getValue()) continue;
			missingByType.computeIfAbsent("", k -> new LinkedHashMap<>()).merge(e.getKey(), e.getValue() - have, Integer::sum);
		}
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
		Map<String, List<Node>> byType = new LinkedHashMap<>();
		Map<String, List<Node>> blockedByType = new LinkedHashMap<>();
		for (Draft d : drafts.values()) {
			Item itemId = d.target().getItem();
			int t = times.getOrDefault(itemId, 0);
			String type = d.recipe().getType().toString();
			List<Boolean> flags = enoughByNode.getOrDefault(itemId, List.of());
			List<ReplenishEntry> entries = new ArrayList<>(d.items().size());
			for (var i = 0; i < d.items().size(); i++) {
				DraftItem di = d.items().get(i);
				boolean enough = i < flags.size() && flags.get(i);
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
	private static void ensureCacheOwner() {
		RecipeManager manager = mc.level == null ? null : mc.level.getRecipeManager();
		if (manager == cacheOwner) return;
		cacheOwner = manager;
		candidatesCache = new HashMap<>();
		recipeOutputs = null;
	}
	private static boolean createsCycle(Recipe<?> recipe, Item target, Map<Item, Draft> drafts) {
		if (drafts.isEmpty()) return false;
		for (Ingredient ing : recipe.getIngredients()) {
			if (ing.isEmpty()) continue;
			for (ItemStack option : ing.getItems())
				if (drafts.containsKey(option.getItem()) && reaches(drafts, option.getItem(), target, new HashSet<>())) return true;
		}
		return false;
	}
	private static boolean reaches(Map<Item, Draft> drafts, Item from, Item target, Set<Item> visited) {
		if (from.equals(target)) return true;
		if (!visited.add(from)) return false;
		Draft d = drafts.get(from);
		if (d == null) return false;
		for (DraftItem di : d.items())
			if (reaches(drafts, di.material().getItem(), target, visited)) return true;
		return false;
	}
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
	private static int avail(Map<Item, Integer> pool, ItemStack material, InventorySummary summary) {
		return pool.computeIfAbsent(material.getItem(), k -> summary.getCountOf(k.getDefaultInstance()));
	}
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
	private static List<RecipeHolder<?>> candidates(ItemStack target, InventorySummary summary) {
		return candidatesCache.computeIfAbsent(target.getItem(), k -> candidatesFor(target, summary));
	}
	private static List<CandidateCard> candidateCards(ItemStack target, InventorySummary summary) {
		List<RecipeHolder<?>> holders = candidates(target, summary);
		List<CandidateCard> out = new ArrayList<>(holders.size());
		for (RecipeHolder<?> holder : holders) out.add(new CandidateCard(holder, entriesFor(holder.value(), summary)));
		return out;
	}
	private static List<ReplenishEntry> entriesFor(Recipe<?> recipe, InventorySummary summary) {
		List<DraftItem> items = draftItems(recipe, summary);
		List<ReplenishEntry> out = new ArrayList<>(items.size());
		for (DraftItem di : items)
			out.add(new ReplenishEntry(di.material(), di.per(), summary.getCountOf(di.material()) >= di.per(), hasRecipe(di.material())));
		return out;
	}
	public static List<RecipeHolder<?>> candidatesFor(ItemStack target, InventorySummary summary) {
		if (mc.level == null) return List.of();
		RegistryAccess registry = mc.level.registryAccess();
		List<String> typeOrder = CCG.config.misc.replenishTypeOrder;
		List<Candidate> found = new ArrayList<>();
		outer:
		for (RecipeHolder<?> holder : mc.level.getRecipeManager().getRecipes()) {
			Recipe<?> recipe = holder.value();
			if (recipe.getIngredients().isEmpty()) continue;
			ItemStack result = recipe.getResultItem(registry);
			if (result.isEmpty() || result.getItem() != target.getItem()) continue;
			for (Ingredient ing : recipe.getIngredients())
				if (!ing.isEmpty() && ing.test(target)) {
					LOGGER.debug("跳过自循环配方 {} (产物 {} 也是原料)", recipe.getType(), target.getHoverName().getString());
					continue outer;
				}
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
			String typeId = recipe.getType().toString();
			int rank = typeOrder.indexOf(typeId);
			if (rank < 0) rank = typeId.startsWith("create:") ? typeOrder.size() : typeOrder.size() + 1;
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
	private static Recipe<?> findAnyRecipeFor(ItemStack target, InventorySummary summary, Map<Item, Draft> drafts) {
		if (mc.level == null) return null;
		Item key = target.getItem();
		ResourceLocation chosen = RECIPE_CHOICES.get(key);
		if (chosen != null) {
			Recipe<?> forced = mc.level.getRecipeManager().byKey(chosen).map(RecipeHolder::value).orElse(null);
			if (forced != null) return forced;
		}
		List<RecipeHolder<?>> candidates = candidates(target, summary);
		for (RecipeHolder<?> holder : candidates)
			if (!createsCycle(holder.value(), key, drafts)) {
				LOGGER.debug("选中配方 {} (候选 {} 个)", holder.value().getType(), candidates.size());
				return holder.value();
			}
		if (!candidates.isEmpty())
			LOGGER.debug("候选配方全部成环，{} 视为原材料 (候选 {} 个)", target.getHoverName().getString(), candidates.size());
		return null;
	}
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
	private record Draft(ItemStack target, Recipe<?> recipe, int outPer, int want, int shortfall, List<DraftItem> items) {}
	private record DraftItem(ItemStack material, int per) {}
}
