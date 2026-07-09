package io.github.forgestove.create_cyber_goggles.core.util;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.FontAccessor;
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
		var alpha = color >> 24 & 255;
		var red = color >> 16 & 255;
		var green = color >> 8 & 255;
		var blue = color & 255;
		var darkenFactor = 0.22F;
		red = (int) ((float) red * darkenFactor) & 255;
		green = (int) ((float) green * darkenFactor) & 255;
		blue = (int) ((float) blue * darkenFactor) & 255;
		var shadowColor = alpha << 24 | red << 16 | green << 8 | blue;
		var matrix4f = new Matrix4f(matrix);
		matrix4f.translate(0F, 0F, 0.1F);
		var accessor = (FontAccessor) font;
		for (var dx = -1; dx <= 1; dx++)
			for (var dy = -1; dy <= 1; dy++)
				if (dx != 0 || dy != 0) accessor.callDrawInternal(
					text,
					x + dx,
					y + dy,
					shadowColor,
					false,
					matrix4f,
					buffer,
					DisplayMode.NORMAL,
					0,
					packedLightCoords
				);
		accessor.callDrawInternal(text, x, y, color, false, matrix4f, buffer, DisplayMode.POLYGON_OFFSET, 0, packedLightCoords);
		cir.setReturnValue(font.width(text) + 1);
	}
}
