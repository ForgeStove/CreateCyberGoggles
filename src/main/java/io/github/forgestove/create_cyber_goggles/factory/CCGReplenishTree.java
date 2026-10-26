package io.github.forgestove.create_cyber_goggles.factory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;
/** 「自动补齐」界面回宿主屏幕的接口；实现方是各宿主屏幕的 mixin（mixin 不在屏幕继承链上，只能靠接口回调）。 */
public interface CCGReplenishTree {
	/** 按当前的配方选择重建整棵依赖树。 */
	List<ReplenishGroup> ccg$buildGroups();
	/** 指定某物品用哪个配方，{@code null} 恢复自动选优。 */
	void ccg$setRecipeChoice(Item item, ResourceLocation recipeId);
	/** 宿主当前的仓库库存快照，未收到时为 null。 */
	@Nullable InventorySummary ccg$summary();
	/** 按宿主自己的通道发出一条补货订单；地址为空时是否跳过由宿主决定。 */
	void ccg$sendOrder(PackageOrderWithCrafts order, String address);
}
