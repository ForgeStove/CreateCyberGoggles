package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.LogisticalStockResponsePacket;
import io.github.forgestove.create_cyber_goggles.core.api.StockSnapshotHolder;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
/** 原响应包只把库存交给 StockTicker，此处让红石请求器等实现了 StockSnapshotHolder 的实体也接收 */
@Mixin(LogisticalStockResponsePacket.class)
public abstract class LogisticalStockResponsePacketMixin {
	@Inject(method = "handle", at = @At("HEAD"), cancellable = true)
	private void handleRedstoneRequester(LocalPlayer player, CallbackInfo ci) {
		if (mc.level == null || !(mc.level.getBlockEntity(pos()) instanceof StockSnapshotHolder holder)) return;
		holder.ccg$receiveStockPacket(items(), lastPacket());
		ci.cancel();
	}
	@Shadow
	public abstract BlockPos pos();
	@Shadow
	public abstract List<BigItemStack> items();
	@Shadow
	public abstract boolean lastPacket();
}
