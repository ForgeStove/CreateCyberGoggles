package io.github.forgestove.create_cyber_goggles.mixin.compact;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.ItemCountFontUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Pseudo
@Mixin(StorageScreenBase.class)
public class StorageScreenBaseMixin {
	@Inject(method = "renderStackCount", at = @At("HEAD"), cancellable = true)
	public void renderStackCount(GuiGraphics gui, String count, int x, int y, CallbackInfo ci) {
		if (!CCG.config.misc.createStyleCount) return;
		var component = ItemCountFontUtil.getStyledAmount(count);
		ItemCountFontUtil.renderSizeLabel(gui, mc.font, x, y, component);
		ci.cancel();
	}
}
