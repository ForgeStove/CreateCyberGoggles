package io.github.forgestove.create_cyber_goggles.mixin.misc;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.ItemCountFontUtil;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Font.class)
public abstract class FontMixin implements Self<Font> {
	@Inject(
		method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;"
			+ "Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I",
		at = @At("HEAD"),
		cancellable = true
	)
	public void drawInBatch(
		FormattedCharSequence text,
		float x,
		float y,
		int color,
		boolean dropShadow,
		Matrix4f matrix,
		MultiBufferSource buffer,
		DisplayMode displayMode,
		int backgroundColor,
		int packedLightCoords,
		CallbackInfoReturnable<Integer> cir
	) {
		if (!CCG.config.misc.createStackCount.enableCreateStyleStackCount) return;
		ItemCountFontUtil.drawOutline(thiz(), text, x, y, color, dropShadow, matrix, buffer, packedLightCoords, cir);
	}
}
