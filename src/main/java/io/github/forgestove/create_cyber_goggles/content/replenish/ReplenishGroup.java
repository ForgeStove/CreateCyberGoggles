package io.github.forgestove.create_cyber_goggles.content.replenish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.List;
/**
 * 自动补齐的配方类型组。
 *
 * @param name    配方类型 id
 * @param nodes   可合成的节点
 * @param blocked 缺料无法合成的节点
 * @param missing 该类型缺少的原料
 */
public record ReplenishGroup(String name, List<Node> nodes, List<Node> blocked, List<ItemStack> missing) {
	/**
	 * 待合成物品节点。
	 *
	 * @param wantTimes  目标合成次数
	 * @param craftTimes 实际可合成次数
	 * @param items      单次配方的原料清单
	 * @param depth      依赖深度
	 */
	public record Node(
		ItemStack target,
		int wantTimes,
		Recipe<?> recipe,
		int craftTimes,
		List<ReplenishEntry> items,
		int depth,
		List<CandidateCard> candidates
	) {}
	public record CandidateCard(RecipeHolder<?> holder, List<ReplenishEntry> items) {}
	/**
	 * 原料条目。
	 *
	 * @param material  数量恒为 1
	 * @param per       单次配方用量
	 * @param enough    库存满足一次配方
	 * @param craftable 是否为配方产物
	 */
	public record ReplenishEntry(ItemStack material, int per, boolean enough, boolean craftable) {}
}
