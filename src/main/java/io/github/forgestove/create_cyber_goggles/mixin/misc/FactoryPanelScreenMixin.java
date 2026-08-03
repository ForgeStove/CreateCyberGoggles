package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.gui.AbstractSimiScreen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.getModifiedScrollAmount;
@Mixin(FactoryPanelScreen.class)
public abstract class FactoryPanelScreenMixin extends AbstractSimiScreen {
	@Shadow private boolean restocker;
	@Shadow private IconButton relocateButton;
	@ModifyConstant(method = "mouseScrolled", constant = @Constant(intValue = 64))
	public int modifyMaxScrollAmount(int original) {
		return CCG.config.misc.removeRequestLimit ? Integer.MAX_VALUE : original;
	}
	/** 步进量：shift +64 / ctrl +10，值恰为 1 时 -1 对齐 64 / 10 */
	@ModifyArg(method = "mouseScrolled", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"), index = 0)
	public int modifyPerScrollAmount(
		int original,
		@Local(name = "itemStack") BigItemStack itemStack,
		@Local(name = "scrollY") double scrollY
	) {
		if (!CCG.config.misc.removeRequestLimit) return original;
		var amount = getModifiedScrollAmount();
		if (itemStack.count == 1 && amount > 1) amount--;
		return (int) (itemStack.count + Math.signum(scrollY) * amount);
	}
	@Inject(method = "init", at = @At("TAIL"))
	public void init(CallbackInfo ci) {
		if (!CCG.config.goggles.betterFactoryGauge) return;
		if (!restocker) return;
		relocateButton.setPosition(relocateButton.getX() - 23, relocateButton.getY() - 54);
		addRenderableWidget(relocateButton);
	}
}
