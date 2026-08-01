package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import io.github.forgestove.create_cyber_goggles.api.StockSnapshotHolder;
import org.spongepowered.asm.mixin.*;

import java.util.*;
/** 给红石请求器提供客户端网络库存快照（服务端通过 LogisticalStockResponsePacket 回复） */
@Mixin(RedstoneRequesterBlockEntity.class)
public abstract class RedstoneRequesterStockMixin implements StockSnapshotHolder {
	@Unique private InventorySummary ccg$clientsideStockSnapshot;
	@Unique private List<BigItemStack> ccg$newlyReceivedStock;
	@Override
	public InventorySummary ccg$getStockSnapshot() {
		return ccg$clientsideStockSnapshot;
	}
	@Override
	public void ccg$receiveStockPacket(List<BigItemStack> items, boolean lastPacket) {
		if (ccg$newlyReceivedStock == null) ccg$newlyReceivedStock = new ArrayList<>();
		ccg$newlyReceivedStock.addAll(items);
		if (!lastPacket) return;
		ccg$clientsideStockSnapshot = new InventorySummary();
		ccg$newlyReceivedStock.forEach(bigStack -> ccg$clientsideStockSnapshot.add(bigStack));
		ccg$newlyReceivedStock = null;
	}
}
