package io.github.forgestove.create_cyber_goggles.api;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;

import java.util.List;
/** 客户端持有网络库存快照的方块实体（红石请求器），供 JEI 转移按网络库存选择原料 */
public interface StockSnapshotHolder {
	InventorySummary ccg$getStockSnapshot();
	void ccg$receiveStockPacket(List<BigItemStack> items, boolean lastPacket);
}
