package io.github.forgestove.create_cyber_goggles.mixin.compact.createPhantom;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.yision.phantom.item.ticker.*;
import com.yision.phantom.network.ticker.TunablePortableTickerSendOrderPacket;
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

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
/**
 * 给 phantom 的可调便携仓储管理员（{@link TunablePortableTickerScreen}）补两个功能，都只在主手（其次副手）拿着 Create 剪贴板时启用：
 * 蓝图模式（等库存到位后按 {@code requestSchematicList()} 的语义照单填单，不递归配方）与自动补齐缺货按钮
 * （受 {@code misc.autoReplenishStock} 控制，递归到原材料，求解交给 {@link ReplenishPlanner}）。
 */
@Pseudo
@Mixin(TunablePortableTickerScreen.class)
public abstract class TunablePortableTickerScreenMixin extends AbstractSimiContainerScreen<TunablePortableTickerMenu>
	implements Self<TunablePortableTickerScreen>, CCGReplenishTree {
	/**
 * 正在打开本覆盖层。宿主的 {@code removed()} 会调 {@code ClientScreenStorage.close()} 清空库存缓存，
 * 而被覆盖层顶掉也算一次 removed，故标记这一次不清缓存。
 */
	@Unique private static boolean ccg$overlayOpening;
	@Shadow public List<BigItemStack> itemsToOrder;
	@Shadow private int activeChannel;
	@Shadow private UUID activeSessionNetwork;
	@Shadow private List<BigItemStack> lastSeenStacks;
	/** 蓝图模式待填单；只填一次，之后不覆盖玩家手改的订单。 */
	@Unique private boolean ccg$schematicPending;
	public TunablePortableTickerScreenMixin(TunablePortableTickerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}
	@Inject(method = "init", at = @At("TAIL"))
	private void portableReplenishInit(CallbackInfo ci) {
		if (ReplenishPlanner.clipboardOf(mc.player) == null) return; // 没拿剪贴板则两个功能都不启用
		ccg$schematicPending = true;
		if (!CCG.config.misc.autoReplenishStock) return;
		// y 比 Create 仓管高一行：≥2 个网络时宿主会在右侧前景绘制通道栏，会盖住 widget
		var btn = new IconButton(leftPos + imageWidth - 10, topPos + 8, AllIcons.I_ADD);
		btn.withCallback(() -> {
			List<ReplenishGroup> groups = ccg$buildGroups();
			CCG.LOGGER.debug("AutoReplenish click: groups={} summary={}", groups.size(), lastSeenStacks == null ? "null" : "ok");
			ccg$overlayOpening = true; // 本次切屏会调宿主的 removed()，别清库存缓存
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
		var summary = new InventorySummary();
		if (lastSeenStacks != null) summary.addAllBigItemStacks(lastSeenStacks);
		return summary;
	}
	@WrapOperation(
		method = "removed", at = @At(
		value = "INVOKE", target = "Lcom/yision/phantom/item/ticker/ClientScreenStorage;close()V"
	)
	)
	private void ccg$keepStockOnOverlay(Operation<Void> original) {
		if (ccg$overlayOpening) {
			ccg$overlayOpening = false;
			return;
		}
		original.call();
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
		itemsToOrder.addAll(orders);
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
		if (activeSessionNetwork == null) {   // 未绑定物流网络：宿主自己的发送路径同样会跳过
			CCG.LOGGER.debug("可调便携仓储管理员未绑定网络, 跳过发送");
			return;
		}
		CatnipServices.NETWORK.sendToServer(new TunablePortableTickerSendOrderPacket(
			menu.locator,
			activeChannel,
			activeSessionNetwork,
			order,
			address
		));
	}
}
