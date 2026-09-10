package io.github.forgestove.create_cyber_goggles.core.factory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;
/**
 * 「自动补齐」界面回宿主屏幕的接口：切换配方后要按新的选择重建整棵依赖树。
 * 实现方是 {@code StockKeeperReplenishEntryMixin}（它就是宿主屏幕本身，mixin 不在屏幕的继承链上，只能靠接口回调）。
 */
public interface CCGReplenishTree {
	/** 重新构建整棵依赖树（使用当前的配方选择） */
	List<ReplenishGroup> ccg$buildGroups();
	/** 指定某物品用哪个配方（{@code null} = 恢复自动选优） */
	void ccg$setRecipeChoice(Item item, ResourceLocation recipeId);
}
