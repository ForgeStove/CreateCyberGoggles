package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import io.github.forgestove.create_cyber_goggles.core.factory.RequestAmountScreen;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.createmod.catnip.gui.AbstractSimiScreen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(FactoryPanelScreen.class)
public abstract class FactoryPanelScreenMixin extends AbstractSimiScreen implements Self<FactoryPanelScreen> {
	@Shadow private boolean restocker;
	@Shadow private boolean craftingActive;
	@Shadow private List<BigItemStack> inputConfig;
	@Shadow private IconButton relocateButton;
	@Shadow private BigItemStack outputConfig;
	@ModifyConstant(method = "mouseScrolled", constant = @Constant(intValue = 64))
	public int modifyMaxScrollAmount(int original) {
		return CCG.config.misc.removeRequestLimit ? Integer.MAX_VALUE : original;
	}
	@ModifyArg(method = "mouseScrolled", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"), index = 0)
	public int modifyScrollAmount(
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
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	public void openPopup(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (CCG.config.misc.quickRequestActions && CCGKey.stockRequestSetter.isDown() && ccg$openPopupForHoveredSlot(mouseX, mouseY))
			cir.setReturnValue(true);
	}
	@Unique
	private boolean ccg$openPopupForHoveredSlot(double mouseX, double mouseY) {
		if (craftingActive) return false;
		// 输出格（仅非 restocker 模式显示，原版位置 x+160, y+48）
		if (!restocker && !outputConfig.stack.isEmpty()) {
			var outputX = guiLeft + 160;
			var outputY = guiTop + 48;
			if (mouseX >= outputX && mouseX < outputX + 16 && mouseY >= outputY && mouseY < outputY + 16) {
				var max = CCG.config.misc.removeRequestLimit ? Integer.MAX_VALUE : 64;
				mc.setScreen(new RequestAmountScreen(
					thiz(),
					outputConfig.stack,
					outputConfig.count,
					max,
					count -> outputConfig.count = count
				));
				return true;
			}
		}
		for (var i = 0; i < inputConfig.size(); i++) {
			var inputX = guiLeft + (restocker ? 88 : 68 + i % 3 * 20);
			var inputY = guiTop + (restocker ? 12 : 28) + i / 3 * 20;
			if (mouseX < inputX || mouseX >= inputX + 16 || mouseY < inputY || mouseY >= inputY + 16) continue;
			var itemStack = inputConfig.get(i);
			if (itemStack.stack.isEmpty()) return false;
			var max = CCG.config.misc.removeRequestLimit ? Integer.MAX_VALUE : 64;
			mc.setScreen(new RequestAmountScreen(thiz(), itemStack.stack, itemStack.count, max, count -> itemStack.count = count));
			return true;
		}
		return false;
	}
}
