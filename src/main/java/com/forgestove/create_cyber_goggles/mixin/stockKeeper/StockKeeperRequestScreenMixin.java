package com.forgestove.create_cyber_goggles.mixin.stockKeeper;
import com.simibubi.create.content.logistics.*;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperRequestScreenMixin extends AbstractSimiContainerScreen<StockKeeperRequestMenu> {
	@Shadow(remap = false) public AddressEditBox addressBox;
	@Shadow(remap = false) public List<List<BigItemStack>> displayedItems;
	@Shadow(remap = false) public List<BigItemStack> itemsToOrder;
	@Shadow(remap = false) public List<List<BigItemStack>> currentItemSource;
	@Shadow(remap = false) public boolean refreshSearchNextTick;
	@Shadow(remap = false) public LerpedFloat itemScroll;
	@Shadow(remap = false) public boolean moveToTopNextTick;
	@Shadow(remap = false) int emptyTicks;
	@Shadow(remap = false) int successTicks;
	@Shadow(remap = false) StockTickerBlockEntity blockEntity;
	@Shadow(remap = false) private InventorySummary forcedEntries;
	public StockKeeperRequestScreenMixin(StockKeeperRequestMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	@Inject(method = "containerTick", at = @At("HEAD"), remap = false, cancellable = true)
	protected void containerTick(@NotNull CallbackInfo callbackInfo) {
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
	@Shadow(remap = false)
	private void revalidateOrders() {}
	@Shadow(remap = false)
	private void refreshSearchResults(boolean scrollBackUp) {}
}
