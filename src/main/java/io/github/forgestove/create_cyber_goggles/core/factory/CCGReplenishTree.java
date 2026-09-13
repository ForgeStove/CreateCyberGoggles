package io.github.forgestove.create_cyber_goggles.core.factory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;
/**
 * 「自动补齐」界面回宿主屏幕的接口：宿主提供库存快照、发订单通道，并在切换配方后按新的选择重建整棵依赖树。
 * 实现方是各宿主屏幕的 mixin（mixin 不在屏幕的继承链上，只能靠接口回调）：
 * Create 仓管方块、CMP 便携仓储管理员、phantom 可调便携仓储管理员。
 */
public interface CCGReplenishTree {
	/** 重新构建整棵依赖树（使用当前的配方选择） */
	List<ReplenishGroup> ccg$buildGroups();
	/** 指定某物品用哪个配方（{@code null} = 恢复自动选优） */
	void ccg$setRecipeChoice(Item item, ResourceLocation recipeId);
	/** 宿主当前的仓库库存快照（未收到快照时为 null） */
	@Nullable InventorySummary ccg$summary();
	/** 按宿主自己的通道发出一条补货订单（地址为空则由宿主决定是否跳过） */
	void ccg$sendOrder(PackageOrderWithCrafts order, String address);
}
