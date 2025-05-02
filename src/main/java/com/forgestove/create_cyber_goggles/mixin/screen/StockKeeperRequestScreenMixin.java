package com.forgestove.create_cyber_goggles.mixin.screen;
import com.simibubi.create.content.logistics.*;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperRequestScreenMixin extends AbstractSimiContainerScreen<StockKeeperRequestMenu> {
	@Shadow public AddressEditBox addressBox;
	@Shadow public List<List<BigItemStack>> displayedItems;
	@Shadow public List<BigItemStack> itemsToOrder;
	@Shadow public List<List<BigItemStack>> currentItemSource;
	@Shadow public boolean refreshSearchNextTick;
	@Shadow public LerpedFloat itemScroll;
	@Shadow public boolean moveToTopNextTick;
	@Shadow int emptyTicks;
	@Shadow int successTicks;
	@Shadow StockTickerBlockEntity blockEntity;
	@Shadow private InventorySummary forcedEntries;
	public StockKeeperRequestScreenMixin(StockKeeperRequestMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	@Inject(method = "containerTick", at = @At("HEAD"), cancellable = true)
	protected void containerTick(CallbackInfo callbackInfo) {
		callbackInfo.cancel();
		super.containerTick();
		addressBox.tick();
		if (!forcedEntries.isEmpty()) {
			var summary = blockEntity.getLastClientsideStockSnapshotAsSummary();
			for (var stack : forcedEntries.getStacks()) {
				var limitedAmount = -stack.count - 1;
				var actualAmount = summary.getCountOf(stack.stack);
				if (actualAmount <= limitedAmount) forcedEntries.erase(stack.stack);
			}
		}
		var allEmpty = true;
		for (var list : displayedItems) allEmpty &= list.isEmpty();
		if (allEmpty) emptyTicks++;
		else emptyTicks = 0;
		if (successTicks > 0 && itemsToOrder.isEmpty()) successTicks++;
		else successTicks = 0;
		var clientStockSnapshot = blockEntity.getClientStockSnapshot();
		if (clientStockSnapshot != currentItemSource) {
			currentItemSource = clientStockSnapshot;
			refreshSearchResults(false);
			revalidateOrders();
		}
		if (refreshSearchNextTick) {
			refreshSearchNextTick = false;
			refreshSearchResults(moveToTopNextTick);
		}
		itemScroll.tickChaser();
		if (Math.abs(itemScroll.getValue() - itemScroll.getChaseTarget()) < 1 / 16f) itemScroll.setValue(itemScroll.getChaseTarget());
		blockEntity.refreshClientStockSnapshot();
	}
	@Shadow
	private void revalidateOrders() {}
	@Shadow
	private void refreshSearchResults(boolean scrollBackUp) {}
}
