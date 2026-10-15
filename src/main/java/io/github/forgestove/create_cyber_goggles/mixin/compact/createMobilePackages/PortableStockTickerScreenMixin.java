package io.github.forgestove.create_cyber_goggles.mixin.compact.createMobilePackages;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import de.theidler.create_mobile_packages.items.portable_stock_ticker.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.factory.*;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.zznty.create_factory_abstractions.generic.support.*;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
/**
 * 给 CMP 的便携仓储管理员（{@link PortableStockTickerScreen}）补两个功能，都只在主手（其次副手）拿着 Create 剪贴板时启用：
 * 蓝图模式（等库存到位后按 {@code requestSchematicList()} 的语义照单填单，不递归配方）与自动补齐缺货按钮
 * （受 {@code misc.autoReplenishStock} 控制，递归到原材料，求解交给 {@link ReplenishPlanner}）。
 */
@Pseudo
@Mixin(PortableStockTickerScreen.class)
public abstract class PortableStockTickerScreenMixin extends AbstractSimiContainerScreen<PortableStockTickerMenu>
	implements Self<PortableStockTickerScreen>, CCGReplenishTree {
	@Shadow public List<BigGenericStack> itemsToOrder;
	/** 蓝图模式待填单；只填一次，之后不覆盖玩家手改的订单。 */
	@Unique private boolean ccg$schematicPending;
	public PortableStockTickerScreenMixin(PortableStockTickerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}
	@Inject(method = "init", at = @At("TAIL"))
	private void portableReplenishInit(CallbackInfo ci) {
		if (ReplenishPlanner.clipboardOf(mc.player) == null) return; // 没拿剪贴板则两个功能都不启用
		ccg$schematicPending = true;
		if (!CCG.config.misc.autoReplenishStock) return;
		var btn = new IconButton(leftPos + imageWidth - 10, topPos + 20, AllIcons.I_ADD);
		btn.withCallback(() -> {
			List<ReplenishGroup> groups = ccg$buildGroups();
			CCG.LOGGER.debug("AutoReplenish click: groups={} summary={}", groups.size(), ccg$summary() == null ? "null" : "ok");
			mc.setScreen(new ReplenishScreen(thiz(), this, groups));
		});
		btn.setToolTip(Component.translatable("create_cyber_goggles.gui.auto_replenish.title"));
		addRenderableWidget(btn);
	}
	@Unique
	@Override
	public List<ReplenishGroup> ccg$buildGroups() {
		return ReplenishPlanner.build(ReplenishPlanner.clipboardOf(mc.player), ccg$summary());
	}
	@Unique
	@Override
	public InventorySummary ccg$summary() {
		return thiz().stockSnapshot().asSummary();
	}
	/** 蓝图模式：等第一份非空库存到位，把清单里仓库有的物品按 min(清单数量, 库存) 铺进订单列表，只做一次。 */
	@Inject(method = "containerTick", at = @At("TAIL"))
	private void applySchematicOrder(CallbackInfo ci) {
		if (!ccg$schematicPending) return;
		var summary = ccg$summary();
		if (summary != null && summary.isEmpty()) return;
		ccg$schematicPending = false;
		var orders = ReplenishPlanner.schematicOrders(ReplenishPlanner.clipboardOf(mc.player), summary);
		if (orders.isEmpty()) return;
		itemsToOrder.clear();
		itemsToOrder.addAll(orders.stream().map(BigGenericStack::of).toList());
		CCG.LOGGER.debug("SchematicList: 便携界面按剪贴板清单填单 {} 项", orders.size());
	}
	@Unique
	@Override
	public void ccg$setRecipeChoice(Item item, ResourceLocation recipeId) {
		ReplenishPlanner.setRecipeChoice(item, recipeId);
	}
	@Unique
	@Override
	public void ccg$sendOrder(PackageOrderWithCrafts order, String address) {
		CatnipServices.NETWORK.sendToServer(new SendPackage(GenericOrder.of(order), address));
	}
}
