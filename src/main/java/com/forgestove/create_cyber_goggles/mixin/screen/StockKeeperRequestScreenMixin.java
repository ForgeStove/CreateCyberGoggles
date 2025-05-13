package com.forgestove.create_cyber_goggles.mixin.screen;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperRequestScreenMixin {
	@Inject(
		method = "containerTick", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/content/logistics/stockTicker/StockTickerBlockEntity;refreshClientStockSnapshot()V",
		shift = Shift.AFTER
	), cancellable = true
	)
	protected void containerTick(CallbackInfo callbackInfo) {
		callbackInfo.cancel();
	}
}
