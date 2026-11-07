package io.github.forgestove.create_cyber_goggles.content.replenish;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;
/**
 * 宿主屏幕为「自动补齐」界面提供的接口。
 * <p>
 * 实现方是各宿主屏幕的 mixin；mixin 不在屏幕继承链上，因此通过接口回调。
 */
public interface CCGReplenishTree {
	List<ReplenishGroup> ccg$buildGroups();
	/** {@code null} 恢复自动选优。 */
	void ccg$setRecipeChoice(Item item, ResourceLocation recipeId);
	/** 未收到时为 {@code null}。 */
	@Nullable InventorySummary ccg$summary();
	/** 按宿主自己的通道发送补货订单；地址为空时是否跳过由宿主决定。 */
	void ccg$sendOrder(PackageOrderWithCrafts order, String address);
}
