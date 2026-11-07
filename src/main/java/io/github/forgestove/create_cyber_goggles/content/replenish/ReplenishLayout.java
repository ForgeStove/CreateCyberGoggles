package io.github.forgestove.create_cyber_goggles.content.replenish;
import net.minecraft.world.item.ItemStack;

import java.util.*;
class ReplenishLayout {
	static final int PANEL_W = 256;
	static final int HEADER_H = 16;
	static final int FOOTER_H = 47;
	static final int CONTENT_L = 36;
	static final int CONTENT_R = 228;
	static final int NODE_H = 20;
	static final int BP_PAD = 2;
	static final int NODES_PER_ROW = 9;
	static final int CELL_W = 20;
	static final int GROUP_H = 20;
	static final int GROUP_INDENT = 3;
	static final int ADDR_H = 20;
	static final int GROUP_GAP = 0;
	static final int SEND_UP_H = 21;
	static final int SEND_MID_H = 17;
	static final int SEND_DOWN_H = 11;
	static final int SEND_CELL_W = 17;
	static final int SEND_ITEM_Y = SEND_UP_H - 16;
	static final int SEND_LABEL_H = 12;
	static final int SCROLL_W = 5;
	static final int BODY_TOP = HEADER_H;
	static final int ADDR_X = CONTENT_L;
	static final int ADDR_TEXT_IN = 12;
	private final List<Row> rows = new ArrayList<>();
	private final int contentH;
	ReplenishLayout(List<ReplenishGroup> groups, List<List<ItemStack>> sendItems, Set<Integer> hiddenGroups) {
		int y = BODY_TOP;
		for (var gi = 0; gi < groups.size(); gi++) {
			ReplenishGroup g = groups.get(gi);
			rows.add(new Row(y, GROUP_H, RowKind.GROUP, gi));
			y += GROUP_H;
			if (hiddenGroups.contains(gi)) continue;
			int nodeRows = (g.nodes().size() + NODES_PER_ROW - 1) / NODES_PER_ROW;
			if (nodeRows > 0) {
				int h = nodeRows * NODE_H + BP_PAD * 2;
				rows.add(new Row(y, h, RowKind.NODE, gi));
				y += h;
			}
			int blockedRows = (g.blocked().size() + NODES_PER_ROW - 1) / NODES_PER_ROW;
			if (blockedRows > 0) {
				int h = SEND_LABEL_H + blockedRows * NODE_H + BP_PAD * 2;
				rows.add(new Row(y, h, RowKind.BLOCKED, gi));
				y += h;
			}
			if (!g.missing().isEmpty()) {
				int missingLines = (g.missing().size() + NODES_PER_ROW - 1) / NODES_PER_ROW;
				int missingH = SEND_LABEL_H + SEND_UP_H + SEND_DOWN_H + (missingLines - 1) * SEND_MID_H;
				rows.add(new Row(y, missingH, RowKind.MISSING, gi));
				y += missingH;
			}
			List<ItemStack> send = sendItems.get(gi);
			if (!send.isEmpty()) {
				int sendLines = Math.max(1, (send.size() + NODES_PER_ROW - 1) / NODES_PER_ROW);
				int sendH = SEND_LABEL_H + SEND_UP_H + SEND_DOWN_H + (sendLines - 1) * SEND_MID_H;
				rows.add(new Row(y, sendH, RowKind.SEND, gi));
				y += sendH;
			}
			rows.add(new Row(y, ADDR_H, RowKind.ADDR, gi));
			y += ADDR_H + GROUP_GAP;
		}
		contentH = y;
	}
	List<Row> rows() {
		return rows;
	}
	int contentH() {
		return contentH;
	}
	record Row(int y, int h, RowKind kind, int gi) {}
	enum RowKind {
		GROUP,
		NODE,
		BLOCKED,
		SEND,
		MISSING,
		ADDR
	}
}
