package io.github.forgestove.create_cyber_goggles.content.replenish;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts.CraftingEntry;
import io.github.forgestove.create_cyber_goggles.content.replenish.ReplenishGroup.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.slf4j.Logger;

import java.util.*;

class ReplenishSender {
	private static final Logger LOGGER = LogUtils.getLogger();
	static void sendAll(CCGReplenishTree host, List<ReplenishGroup> groups, List<String> addresses) {
		Map<Item, Integer> remaining = new HashMap<>();
		InventorySummary summary = host.ccg$summary();
		for (var i = 0; i < groups.size(); i++) {
			String addr = addresses.get(i);
			for (Node n : groups.get(i).nodes()) {
				if (n.craftTimes() <= 0 || n.items().isEmpty()) continue;
				if (addr == null || addr.isBlank()) {
					LOGGER.debug("未填地址, 跳过 {}", n.target().getHoverName().getString());
					continue;
				}
				if (!hasMaterials(n, remaining, summary)) {
					LOGGER.debug("原料不足, 跳过 {}", n.target().getHoverName().getString());
					continue;
				}
				List<BigItemStack> order = new ArrayList<>();
				for (ReplenishEntry e : n.items())
					order.add(new BigItemStack(e.material().copy(), e.per() * n.craftTimes()));
				List<BigItemStack> pattern = new ArrayList<>();
				if (n.recipe() instanceof CraftingRecipe cr)
					pattern = FactoryPanelScreen.convertRecipeToPackageOrderContext(cr, order, true);
				else for (ReplenishEntry e : n.items()) pattern.add(new BigItemStack(e.material().copy(), e.per()));
				var packet = new PackageOrderWithCrafts(
					new PackageOrder(order),
					List.of(new CraftingEntry(new PackageOrder(pattern), n.craftTimes()))
				);
				LOGGER.debug(
					"Send: {} addr={} craft={}/{} order={}",
					n.target().getHoverName().getString(),
					addr,
					n.craftTimes(),
					n.wantTimes(),
					order
				);
				host.ccg$sendOrder(packet, addr);
			}
		}
	}
	private static boolean hasMaterials(Node n, Map<Item, Integer> remaining, InventorySummary summary) {
		if (summary == null) return false;
		for (ReplenishEntry e : n.items()) {
			Item item = e.material().getItem();
			int have = remaining.computeIfAbsent(item, k -> summary.getCountOf(k.getDefaultInstance()));
			if (have < e.per() * n.craftTimes()) return false;
		}
		for (ReplenishEntry e : n.items()) remaining.merge(e.material().getItem(), -e.per() * n.craftTimes(), Integer::sum);
		return true;
	}
}
