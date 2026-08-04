package io.github.forgestove.create_cyber_goggles.core.event;
import net.minecraft.client.renderer.Rect2i;

import java.util.*;
/**
 * HUD overlay 屏幕区域协调器：记录每帧各 overlay 已占用的矩形，供后续 overlay 查询避让，避免相互重叠。
 * <p>
 * 帧内渲染顺序（从下到上）：GoggleOverlayRenderer → TooltipOverlay → TooltipRenderer。
 */
public final class OverlayManager {
	private static final List<Rect2i> occupied = new ArrayList<>();
	/** 整体下移量：最靠顶的 overlay 触顶时，三个 overlay 一起下移 */
	public static int overallOffsetY;
	/** 整体缩放：最靠底的 overlay 触底时，三个 overlay 一起缩放 */
	public static float overallScale = 1;
	/** 上一帧整体下移量/缩放：clearFrame 时保存，供先渲染的 Goggle/TooltipOverlay 使用 */
	public static int prevOverallOffsetY;
	public static float prevOverallScale = 1;
	/** 整体缩放锚点（三个 overlay 包围盒中心），整体缩放围绕此点 */
	public static int scaleCenterX, scaleCenterY;
	/** 上面 overlay（TooltipOverlay/itemtooltip）占据的最大底部 y；prev 为上一帧值，供 Goggle（最下）避让 */
	public static int upperBottom, prevUpperBottom;
	public static void clearFrame() {
		prevUpperBottom = upperBottom;
		prevOverallOffsetY = overallOffsetY;
		prevOverallScale = overallScale;
		upperBottom = 0;
		overallOffsetY = 0;
		overallScale = 1;
		occupied.clear();
	}
	public static void occupy(int x, int y, int width, int height) {
		if (width <= 0 || height <= 0) return;
		occupied.add(new Rect2i(x, y, width, height));
	}
	/** @return 已占用的矩形列表 */
	public static List<Rect2i> getOccupied() {
		return occupied;
	}
}
