package io.github.forgestove.create_cyber_goggles.core.event;
/**
 * HUD overlay 底部占用登记：记录上面 overlay（TooltipOverlay/itemtooltip）占据的最大底部 y，
 * 供最下的 GoggleOverlay 避让。帧内渲染顺序（从下到上）：GoggleOverlayRenderer → TooltipOverlay → TooltipRenderer。
 */
public final class OverlayManager {
	/** 上面 overlay 占据的最大底部 y；prev 为上一帧值，供 Goggle（最下）避让 */
	public static int upperBottom, prevUpperBottom;
	public static void clearFrame() {
		prevUpperBottom = upperBottom;
		upperBottom = 0;
	}
}
