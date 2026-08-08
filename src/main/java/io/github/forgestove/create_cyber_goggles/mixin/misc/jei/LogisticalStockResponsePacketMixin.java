package io.github.forgestove.create_cyber_goggles.mixin.misc.jei;
import com.simibubi.create.content.logistics.stockTicker.LogisticalStockResponsePacket;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.compat.jei.StockSnapshotHolder;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
/** 原响应包只把库存交给 StockTicker，此处让红石请求器等实现了 StockSnapshotHolder 的实体也接收 */
@Mixin(LogisticalStockResponsePacket.class)
public abstract class LogisticalStockResponsePacketMixin implements Self<LogisticalStockResponsePacket> {
	@Inject(method = "handle", at = @At("HEAD"), cancellable = true)
	private void handleRedstoneRequester(LocalPlayer player, CallbackInfo ci) {
		if (!CCG.config.misc.redstoneRequesterLargeRequest) return;
		var thiz = thiz();
		if (mc.level == null || !(mc.level.getBlockEntity(thiz.pos()) instanceof StockSnapshotHolder holder)) return;
		holder.ccg$receiveStockPacket(thiz.items(), thiz.lastPacket());
		ci.cancel();
	}
}
