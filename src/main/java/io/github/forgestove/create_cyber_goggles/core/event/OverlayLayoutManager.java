package io.github.forgestove.create_cyber_goggles.core.event;
import net.minecraft.client.renderer.Rect2i;

import java.util.*;
/**
 * HUD overlay 屏幕区域协调器：记录每帧各 overlay 已占用的矩形，供后续 overlay 查询避让，避免相互重叠。
 * <p>
 * 帧内渲染顺序（从下到上）：GoggleOverlayRenderer → TooltipOverlay → TooltipRenderer。
 * 前两者渲染后调用 {@link #occupy} 登记区域，后者渲染前用 {@link #findOverlap} 查询并避让。
 */
public final class OverlayLayoutManager {
	private static final List<Rect2i> occupied = new ArrayList<>();
	/** 整体下移量：最靠顶的 overlay 触顶时，三个 overlay 一起下移 */
	public static int overallOffsetY;
	/** 整体缩放：最靠底的 overlay 触底时，三个 overlay 一起缩放 */
	public static float overallScale = 1;
	/** 整体缩放锚点（三个 overlay 包围盒中心），整体缩放围绕此点 */
	public static int scaleCenterX, scaleCenterY;
	public static void clearFrame() {
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
	/** @return 与给定区域重叠的第一个已占用矩形，若无不重叠则返回 {@code null} */
	public static Rect2i findOverlap(int x, int y, int width, int height) {
		for (var rect : occupied)
			if (rect.getX() < x + width
				&& x < rect.getX() + rect.getWidth()
				&& rect.getY() < y + height
				&& y < rect.getY() + rect.getHeight()) return rect;
		return null;
	}
}
