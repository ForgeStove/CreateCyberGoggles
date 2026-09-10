package io.github.forgestove.create_cyber_goggles.core.factory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
/**
 * 自动补齐「一个配方类型组」。{@code name} = 配方类型（如 create:crafting）；
 * {@code nodes} = 该类型下能合成出来的节点；{@code blocked} = 该类型下因缺料合不出来的节点
 * （{@code craftTimes=0}，界面上单独一行「无法合成的配方」，悬停同样弹配方卡）；
 * {@code missing} = 该类型缺的原料（数量 = 还差多少个）。同类型共用一个地址框（界面组尾），地址按类型持久化。
 */
public record ReplenishGroup(String name, List<Node> nodes, List<Node> blocked, List<ItemStack> missing) {
	/**
	 * 一个待合成物品节点：{@code target}=产物、{@code wantTimes}=目标次数、{@code recipe}=当前选中的配方、
	 * {@code craftTimes}=可合成次数、{@code items}=当前配方的全部原料、{@code depth}=解析深度、
	 * {@code candidates}=该产物的全部候选配方（优先级已排好序，界面点节点可切换；每个候选自带原料清单，
	 * 供弹窗里悬浮该行时按配方卡样式显示）。
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
	/** 一个候选配方 + 它的原料清单：{@code holder}=配方，{@code items}=该配方单次的原料（界面弹窗悬浮用） */
	public record CandidateCard(RecipeHolder<?> holder, List<ReplenishEntry> items) {}
	/** 一种原料：per=单次用量，enough=库存是否 ≥per（够一次配方），craftable=自己可合成（会被继续拆解） */
	public record ReplenishEntry(ItemStack material, int per, boolean enough, boolean craftable) {}
}
