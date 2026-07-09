package io.github.forgestove.create_cyber_goggles.mixin.compact;
import com.hollingsworth.arsnouveau.client.container.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.ItemCountFontUtil;
import net.minecraft.client.gui.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AbstractStorageTerminalScreen.class)
public class AbstractStorageTerminalScreenMixin {
	@Inject(method = "drawStackSize", at = @At("HEAD"), cancellable = true)
	public void drawStackSize(GuiGraphics graphics, Font font, long size, int x, int y, CallbackInfo ci) {
		if (!CCG.config.misc.createStyleCount) return;
		ItemCountFontUtil.renderSizeLabel(graphics, font, x, y, ItemCountFontUtil.getStyledAmount(NumberFormatUtil.formatNumber(size)));
		ci.cancel();
	}
}
