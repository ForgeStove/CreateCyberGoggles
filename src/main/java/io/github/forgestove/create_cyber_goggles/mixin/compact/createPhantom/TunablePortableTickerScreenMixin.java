package io.github.forgestove.create_cyber_goggles.mixin.compact.createPhantom;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.yision.phantom.content.logistics.tunablePortableTicker.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.content.replenish.*;
import io.github.forgestove.create_cyber_goggles.foundation.lang.CCGLang;
import io.github.forgestove.create_cyber_goggles.foundation.util.Self;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.foundation.util.CCGUtil.mc;
@Pseudo
@Mixin(TunablePortableTickerScreen.class)
public abstract class TunablePortableTickerScreenMixin extends AbstractSimiContainerScreen<TunablePortableTickerMenu>
	implements Self<TunablePortableTickerScreen>, ReplenishTree {
	@Unique private static boolean ccg$overlayOpening;
	@Unique private boolean ccg$schematicPending;
	public TunablePortableTickerScreenMixin(TunablePortableTickerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}
	@Inject(method = "init", at = @At("TAIL"))
	private void portableReplenishInit(CallbackInfo ci) {
		if (ReplenishPlanner.clipboardOf(mc.player) == null) return;
		ccg$schematicPending = true;
		if (!CCG.config.misc.autoReplenishStock) return;
		var btn = new IconButton(leftPos + imageWidth - 10, topPos + 8, AllIcons.I_ADD);
		btn.withCallback(() -> {
			List<ReplenishGroup> groups = ccg$buildGroups();
			CCG.LOGGER.debug("AutoReplenish click: groups={}", groups.size());
			ccg$overlayOpening = true;
			mc.setScreen(new ReplenishScreen(thiz(), this, groups));
		});
		btn.setToolTip(CCGLang.translateDirect("gui.auto_replenish.title"));
		addRenderableWidget(btn);
	}
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
		var stockData = menu.getStockData();
		return stockData == null ? new InventorySummary() : stockData.getLastClientsideStockSnapshotAsSummary();
	}
	@Unique
	@Override
	public void ccg$sendOrder(PackageOrderWithCrafts order, String address) {
		var stockData = menu.getStockData();
		if (stockData == null) return;
		CatnipServices.NETWORK.sendToServer(new PackageOrderRequestPacket(stockData.getBlockPos(), order, address, false));
	}
	@WrapOperation(
		method = "removed", at = @At(
		value = "INVOKE",
		target = "Lcom/yision/phantom/content/logistics/tunablePortableTicker/ClientScreenStorage;close"
			+ "(Lcom/yision/phantom/content/logistics/tunablePortableTicker/TunablePortableTickerMenu;)V"
	)
	)
	private void ccg$keepStockOnOverlay(TunablePortableTickerMenu menu, Operation<Void> original) {
		if (ccg$overlayOpening) {
			ccg$overlayOpening = false;
			return;
		}
		original.call(menu);
	}
	@Inject(method = "containerTick", at = @At("TAIL"))
	private void applySchematicOrder(CallbackInfo ci) {
		if (!ccg$schematicPending) return;
		InventorySummary summary = ccg$summary();
		if (summary == null || summary.isEmpty()) return;
		ccg$schematicPending = false;
		List<BigItemStack> orders = ReplenishPlanner.schematicOrders(ReplenishPlanner.clipboardOf(mc.player), summary);
		if (orders.isEmpty()) return;
		var itemsToOrder = ((StockKeeperRequestScreen) (Object) this).itemsToOrder;
		itemsToOrder.clear();
		itemsToOrder.addAll(orders);
		CCG.LOGGER.debug("SchematicList: 便携界面按剪贴板清单填单 {} 项", orders.size());
	}
}
