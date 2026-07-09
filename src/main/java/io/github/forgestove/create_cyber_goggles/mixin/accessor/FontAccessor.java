package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(Font.class)
public interface FontAccessor {
	@SuppressWarnings("UnusedReturnValue")
	@Invoker
	int callDrawInternal(
		FormattedCharSequence text,
		float x,
		float y,
		int color,
		boolean dropShadow,
		Matrix4f matrix,
		MultiBufferSource buffer,
		DisplayMode displayMode,
		int backgroundColor,
		int packedLightCoords
	);
}
