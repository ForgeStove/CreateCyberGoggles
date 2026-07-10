package io.github.forgestove.create_cyber_goggles.mixin.compact;
import appeng.client.gui.me.common.MEStorageScreen;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.ItemCountFontUtil;
import net.minecraft.client.gui.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
@Pseudo
@Mixin(MEStorageScreen.class)
public class MEStorageScreenMixin {
	@WrapOperation(
		method = "renderSlot", at = @At(
		value = "INVOKE",
		target = "Lappeng/client/gui/me/common/StackSizeRenderer;renderSizeLabel(Lnet/minecraft/client/gui/GuiGraphics;"
			+ "Lnet/minecraft/client/gui/Font;FFLjava/lang/String;Z)V",
		ordinal = 0
	)
	)
	public void renderSlot(GuiGraphics gui, Font font, float x, float y, String text, boolean largeFonts, Operation<Void> original) {
		if (!CCG.config.misc.createStyleCount) {
			original.call(gui, font, x, y, text, largeFonts);
			return;
		}
		ItemCountFontUtil.renderSizeLabel(gui, font, x, y, ItemCountFontUtil.getStyledAmount(text));
	}
}
