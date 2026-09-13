package io.github.forgestove.create_cyber_goggles.mixin.compact.sophisticatedcore;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.ItemCountFontUtil;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Pseudo
@SuppressWarnings("UnresolvedMixinReference")
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase")
public class StorageScreenBaseMixin {
	@Inject(method = "renderStackCount", at = @At("HEAD"), cancellable = true)
	public void renderStackCount(GuiGraphics gui, String count, int x, int y, CallbackInfo ci) {
		if (!CCG.config.misc.createStackCount.enableCreateStyleStackCount) return;
		var component = ItemCountFontUtil.getStyledAmount(count);
		ItemCountFontUtil.renderSizeLabel(gui, mc.font, x, y, component);
		ci.cancel();
	}
}
