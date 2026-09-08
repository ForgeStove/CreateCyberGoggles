package io.github.forgestove.create_cyber_goggles.core.factory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;
/**
 * 自动补齐「一个配方类型组」。{@code name} = 配方类型（如 create:crafting）；
 * {@code nodes} = 该类型下所有需合成的物品节点。同类型共用一个地址框（界面组尾），地址按类型持久化。
 */
public record ReplenishGroup(String name, List<Node> nodes) {
	/** 一个待合成物品节点：target=产物、wantTimes=目标次数、recipe、craftTimes=可合成次数、items=全部原料 */
	public record Node(ItemStack target, int wantTimes, Recipe<?> recipe, int craftTimes, List<ReplenishEntry> items) {}
	/** 一种原料：per=单次用量，enough=库存是否 ≥per（够一次配方） */
	public record ReplenishEntry(ItemStack material, int per, boolean enough) {}
}
