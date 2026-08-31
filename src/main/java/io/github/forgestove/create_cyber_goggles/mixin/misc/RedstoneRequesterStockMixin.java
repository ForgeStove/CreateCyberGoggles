package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import io.github.forgestove.create_cyber_goggles.compat.jei.StockSnapshotHolder;
import org.spongepowered.asm.mixin.*;

import java.util.*;
/** 给红石请求器提供客户端网络库存快照（服务端通过 LogisticalStockResponsePacket 回复） */
@Mixin(RedstoneRequesterBlockEntity.class)
public abstract class RedstoneRequesterStockMixin implements StockSnapshotHolder {
	@Unique private InventorySummary ccg$stockSnapshot;
	@Unique private List<BigItemStack> ccg$newlyReceivedStock;
	@Override
	public InventorySummary ccg$getStockSnapshot() {
		return ccg$stockSnapshot;
	}
	@Override
	public void ccg$receiveStockPacket(List<BigItemStack> items, boolean lastPacket) {
		if (ccg$newlyReceivedStock == null) ccg$newlyReceivedStock = new ArrayList<>();
		ccg$newlyReceivedStock.addAll(items);
		if (!lastPacket) return;
		ccg$stockSnapshot = new InventorySummary();
		ccg$stockSnapshot.addAllBigItemStacks(ccg$newlyReceivedStock);
		ccg$newlyReceivedStock = null;
	}
}
