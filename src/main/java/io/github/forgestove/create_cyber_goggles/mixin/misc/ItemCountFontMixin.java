package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.ItemCountFontUtil;
import net.minecraft.client.gui.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(GuiGraphics.class)
public abstract class ItemCountFontMixin {
	@WrapOperation(
		method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"
		)
	)
	public int wrapDrawString(
		GuiGraphics gui,
		Font font,
		String text,
		int strX,
		int strY,
		int color,
		boolean dropShadow,
		Operation<Integer> original,
		@Local(ordinal = 0, argsOnly = true) int itemX
	) {
		if (!CCG.config.misc.createStackCount.enableCreateStyleStackCount)
			return original.call(gui, font, text, strX, strY, color, dropShadow);
		var styled = ItemCountFontUtil.getStyledAmount(text);
		var x = itemX + 16 - font.width(styled);
		return gui.drawString(font, styled, x, strY, color);
	}
}
