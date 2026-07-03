package io.github.forgestove.create_cyber_goggles.config.client.gui.util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.function.*;
public class Highlight {
	private static final float FADE_SPEED = 0.25F;
	private static final float POSITION_SPEED = 0.75F;
	private final IntSupplier indexSupplier;
	private final IntUnaryOperator rowTopGetter;
	private float y = -1;
	private float targetY = -1;
	private float alpha;
	private int lastIndex = -1;
	public Highlight(IntSupplier indexSupplier, IntUnaryOperator rowTopGetter) {
		this.indexSupplier = indexSupplier;
		this.rowTopGetter = rowTopGetter;
	}
	public void tick(GuiGraphics gui, int listX, int listY, int width, int height, int itemHeight, float delta) {
		var alphaInt = (int) (alpha * 48); // 最大透明度 48 (0x30)
		var color = alphaInt << 24 | 0xFFFFFF;
		var top = (int) y - 1;
		var bottom = top + itemHeight;
		// 剪辑到可见区域
		var visibleBottom = listY + height;
		if (top < listY) top = listY;
		if (bottom > visibleBottom) bottom = visibleBottom;
		if (top < bottom) gui.fill(listX, top, listX + width, bottom, color);
		var fadeProgress = 1.0f - (float) Math.pow(1.0 - FADE_SPEED, delta);
		var index = indexSupplier.getAsInt();
		if (index >= 0) {
			var entryTop = rowTopGetter.applyAsInt(index);
			targetY = entryTop;
			// 淡入
			alpha = Mth.lerp(fadeProgress, alpha, 0.95F);
			// 如果第一次悬停，初始化位置
			if (y < 0 || lastIndex < 0) y = entryTop;
			lastIndex = index;
		} else {
			// 淡出
			alpha = Mth.lerp(fadeProgress, alpha, 0.0F);
			lastIndex = -1;
		}
		// 带吸附到目标的平滑位置转换
		if (!(targetY >= 0) || !(y >= 0)) return;
		var positionProgress = 1.0f - (float) Math.pow(1.0 - POSITION_SPEED, delta);
		y = Mth.lerp(positionProgress, y, targetY);
		// 靠近时再快速锁定目标
		if (Math.abs(y - targetY) < 1.0f) y = targetY;
	}
}
