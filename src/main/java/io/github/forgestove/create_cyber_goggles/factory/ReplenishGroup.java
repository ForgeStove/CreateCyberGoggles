package io.github.forgestove.create_cyber_goggles.factory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.List;
/** 自动补齐的一个配方类型组；{@code blocked} 是缺料合不出来的节点，{@code missing} 是该类型还差的原料。同类型共用一个地址框（界面组尾）。 */
public record ReplenishGroup(String name, List<Node> nodes, List<Node> blocked, List<ItemStack> missing) {
	/** 一个待合成物品节点；{@code wantTimes} 是目标次数，{@code craftTimes} 是可合成次数。 */
	public record Node(
		ItemStack target,
		int wantTimes,
		Recipe<?> recipe,
		int craftTimes,
		List<ReplenishEntry> items,
		int depth,
		List<CandidateCard> candidates
	) {}
	/** 一个候选配方 + 它单次的原料清单。 */
	public record CandidateCard(RecipeHolder<?> holder, List<ReplenishEntry> items) {}
	/** 一种原料；{@code enough}=库存够一次配方，{@code craftable}=自己可合成（会被继续拆解）。 */
	public record ReplenishEntry(ItemStack material, int per, boolean enough, boolean craftable) {}
}
