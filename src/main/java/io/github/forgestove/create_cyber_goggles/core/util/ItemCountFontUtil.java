package io.github.forgestove.create_cyber_goggles.core.util;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.getCCGRes;
public final class ItemCountFontUtil {
	public static final ResourceLocation FONT_CREATE = getCCGRes("create");
	public static Component getStyledAmount(Component component) {
		return component.copy().withStyle(Style.EMPTY.withFont(FONT_CREATE));
	}
	public static Component getStyledAmount(String text) {
		return Component.literal(text).withStyle(Style.EMPTY.withFont(FONT_CREATE));
	}
	public static void renderSizeLabel(GuiGraphics gui, Font font, float x, float y, Component text) {
		float fontWidth = font.width(text);
		var scale = Math.min(1F, 15F / fontWidth);
		var stack = gui.pose();
		stack.pushPose();
		stack.translate(0F, 0F, 200F);
		stack.scale(scale, scale, 1F);
		var xP = (int) ((x + 16F) / scale - fontWidth);
		var yP = (int) ((y + 16F) / scale - 7F);
		gui.drawString(font, text, xP, yP, 0xFFFFFF, true);
		stack.popPose();
	}
	public static void drawOutline(
		Font font,
		FormattedCharSequence text,
		float x,
		float y,
		int color,
		boolean dropShadow,
		Matrix4f matrix,
		MultiBufferSource buffer,
		int packedLightCoords,
		CallbackInfoReturnable<Integer> cir
	) {
		var rl = new ResourceLocation[1];
		text.accept((i, style, j) -> {
			rl[0] = style.getFont();
			return false;
		});
		if (rl[0] == null || !rl[0].equals(FONT_CREATE)) return;
		if (!dropShadow) return;
		var shadowColor = CCG.config.misc.createStackCount.countOutlineColor;
		if (!CCGMods.modernui.isLoaded()) font.drawInBatch8xOutline(text, x, y, color, shadowColor, matrix, buffer, packedLightCoords);
		else drawInBatch8xOutline(font, text, x, y, color, shadowColor, matrix, buffer, packedLightCoords);
		cir.setReturnValue(font.width(text) + 1);
	}
	public static void drawInBatch8xOutline(
		Font font,
		FormattedCharSequence text,
		float x,
		float y,
		int color,
		int shadowColor,
		Matrix4f matrix,
		MultiBufferSource buffer,
		int packedLightCoords
	) {
		var finalY = y - 1F;
		var adjusted = Font.adjustColor(shadowColor);
		var outputOutliner = font.new StringRenderOutput(buffer, 0, 0, adjusted, false, matrix, DisplayMode.NORMAL, packedLightCoords);
		for (var dx = -1; dx <= 1; dx++)
			for (var dy = -1; dy <= 1; dy++) {
				if (dx == 0 && dy == 0) continue;
				var cursorX = new float[]{x};
				var finalDx = dx;
				var finalDy = dy;
				text.accept((pos, style, codePoint) -> {
					var fontset = font.getFontSet(style.getFont());
					var glyphinfo = fontset.getGlyphInfo(codePoint, font.filterFishyGlyphs);
					var offset = glyphinfo.getShadowOffset();
					outputOutliner.x = cursorX[0] + finalDx * offset * 2F;
					outputOutliner.y = finalY + finalDy * offset * 2F;
					cursorX[0] += glyphinfo.getAdvance(style.isBold());
					return outputOutliner.accept(pos, style.withColor(adjusted), codePoint);
				});
			}
		var outputInner = font.new StringRenderOutput(
			buffer,
			x,
			finalY,
			Font.adjustColor(color),
			false,
			matrix,
			DisplayMode.POLYGON_OFFSET,
			packedLightCoords
		);
		text.accept(outputInner);
		outputInner.finish(0, x);
	}
}
