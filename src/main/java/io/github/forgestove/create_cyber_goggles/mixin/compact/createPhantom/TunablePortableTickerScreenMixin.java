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
 * 给 phantom 的可调便携仓储管理员（{@link TunablePortableTickerScreen}）补上两个功能，
 * 都只在<b>主手（其次副手）拿着 Create 剪贴板</b>时启用——便携界面多用快捷键打开、物品不必占手，
 * 所以与 Create 仓管的「手持剪贴板蓝图模式」同源：
 * <ul>
 * <li><b>蓝图模式</b>：打开后等第一份库存到位，按 Create {@code requestSchematicList()} 的语义
 *     把清单里仓库有的物品照单填进订单列表（不递归配方）；</li>
 * <li><b>自动补齐缺货</b>按钮（受 {@code misc.autoReplenishStock} 控制）：递归到原材料，求解交给
 *     与宿主无关的 {@link ReplenishPlanner}。</li>
 * </ul>
 * <p>宿主能力：库存快照 = 客户端缓存的库存栈现算（同宿主 {@code getLatestSummary()} 的算法，
 * 但不取 clamp 到 1000 的规划快照）；发订单 = 宿主自己的 {@link TunablePortableTickerSendOrderPacket}。</p>
 */
@Pseudo
@Mixin(TunablePortableTickerScreen.class)
public abstract class TunablePortableTickerScreenMixin extends AbstractSimiContainerScreen<TunablePortableTickerMenu>
	implements Self<TunablePortableTickerScreen>, CCGReplenishTree {
	@Shadow private int activeChannel;
	@Shadow private UUID activeSessionNetwork;
	@Shadow private List<BigItemStack> lastSeenStacks;
	@Shadow public List<BigItemStack> itemsToOrder;
	/** 蓝图模式待填单（等库存到位；只填一次，之后不覆盖玩家手改的订单） */
	@Unique private boolean ccg$schematicPending;
	/**
	 * 正在打开本覆盖层。宿主的 {@code removed()} 会调 {@code ClientScreenStorage.close()} 清空库存缓存，
	 * 而被覆盖层顶掉也算一次 removed（Minecraft 切屏时对旧屏调用）→ 关闭覆盖层后宿主不会再 init，
	 * 库存要等 100 tick 才重新请求，期间界面显示「仓储无内容」。标记这一次不清缓存。
	 */
	@Unique private static boolean ccg$overlayOpening;
	public TunablePortableTickerScreenMixin(TunablePortableTickerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}
	@Inject(method = "init", at = @At("TAIL"))
	private void ccg$portableReplenishInit(CallbackInfo ci) {
		if (ReplenishPlanner.clipboardOf(mc.player) == null) return;   // 主手/副手没拿剪贴板蓝图 → 两个功能都不启用
		ccg$schematicPending = true;                                   // 蓝图模式：等库存到位后照单填单
		if (!CCG.config.misc.autoReplenishStock) return;
		// 与 Create 仓管按钮同一横坐标（leftPos + imageWidth - 10）；y 比 Create 的高一行：≥2 个网络时
		// 窗口右侧从相对 +30 起会画通道栏，且它是前景绘制的（会盖住 widget），按钮必须压在它上面
		var btn = new IconButton(leftPos + imageWidth - 10, topPos + 8, AllIcons.I_ADD);
		btn.withCallback(() -> {
			List<ReplenishGroup> groups = ccg$buildGroups();
			CCG.LOGGER.debug("ccg autoReplenish click: groups={} summary={}", groups.size(), lastSeenStacks == null ? "null" : "ok");
			ccg$overlayOpening = true;   // 本次切屏会调宿主的 removed()，别让它清掉库存缓存
			mc.setScreen(new AutoReplenishScreen(thiz(), this, groups));
		});
		btn.setToolTip(Component.translatable("create_cyber_goggles.gui.auto_replenish.title"));
		addRenderableWidget(btn);
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
	/**
	 * 蓝图模式（Create {@code requestSchematicList()} 的同义）：等第一份非空库存到位，把清单里仓库有的
	 * 物品按 {@code min(清单数量, 库存)} 铺进订单列表。只做一次，之后玩家手改的订单不会被覆盖。
	 */
	@Inject(method = "containerTick", at = @At("TAIL"))
	private void ccg$applySchematicOrder(CallbackInfo ci) {
		if (!ccg$schematicPending) return;
		var summary = ccg$summary();
		if (summary != null && summary.isEmpty()) return;   // 库存还没到，下一 tick 再看
		ccg$schematicPending = false;
		var orders = ReplenishPlanner.schematicOrders(ReplenishPlanner.clipboardOf(mc.player), summary);
		if (orders.isEmpty()) return;
		itemsToOrder.clear();
		itemsToOrder.addAll(orders);
		CCG.LOGGER.debug("ccg schematicList: 便携界面按剪贴板清单填单 {} 项", orders.size());
	}
	/** {@link CCGReplenishTree}：界面切换配方后要重新构建整棵树 */
	@Unique
	@Override
	public List<ReplenishGroup> ccg$buildGroups() {
		return ReplenishPlanner.build(ReplenishPlanner.clipboardOf(mc.player), ccg$summary());
	}
	@Unique
	@Override
	public void ccg$setRecipeChoice(Item item, ResourceLocation recipeId) {
		ReplenishPlanner.setRecipeChoice(item, recipeId);
	}
	@Unique
	@Override
	public InventorySummary ccg$summary() {
		var summary = new InventorySummary();
		if (lastSeenStacks != null) summary.addAllBigItemStacks(lastSeenStacks);
		return summary;
	}
	@Unique
	@Override
	public void ccg$sendOrder(PackageOrderWithCrafts order, String address) {
		if (activeSessionNetwork == null) {   // 未绑定物流网络：宿主自己的发送路径同样会跳过
			CCG.LOGGER.debug("ccg autoReplenish: 可调便携仓储管理员未绑定网络, 跳过发送");
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
