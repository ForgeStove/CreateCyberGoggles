package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import io.github.forgestove.create_cyber_goggles.core.factory.RequestAmountOverlay;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.getModifiedScrollAmount;
@Mixin(FactoryPanelScreen.class)
public abstract class FactoryPanelScreenMixin extends AbstractSimiScreen {
	@Shadow private boolean restocker;
	@Shadow private boolean craftingActive;
	@Shadow private List<BigItemStack> inputConfig;
	@Shadow private IconButton relocateButton;
	/** 自定义请求数量弹窗（复用红石/仓储的 RequestAmountOverlay） */
	@Unique private RequestAmountOverlay ccg$popup = new RequestAmountOverlay();
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
	@Override
	public void resize(@NotNull Minecraft minecraft, int width, int height) {
		super.resize(minecraft, width, height);
		ccg$popup = new RequestAmountOverlay();
	}
	@Inject(method = "renderWindow", at = @At("HEAD"))
	public void renderWindow(GuiGraphics gui, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		if (ccg$popup.open) ccg$popup.render(gui, mouseX, mouseY, partialTicks);
	}
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (ccg$popup.open) {
			ccg$popup.mouseClicked(mouseX, mouseY, button);
			cir.setReturnValue(true);
		}
		if (CCG.config.misc.quickRequestActions && CCGKey.stockRequestSetter.isDown() && ccg$openPopupForHoveredSlot(mouseX, mouseY))
			cir.setReturnValue(true);
	}
	/** 自定义请求数量弹窗：中键/按住中键点击输入格触发 */
	@Unique
	private boolean ccg$openPopupForHoveredSlot(double mouseX, double mouseY) {
		if (craftingActive) return false;
		for (var i = 0; i < inputConfig.size(); i++) {
			var inputX = guiLeft + (restocker ? 88 : 68 + i % 3 * 20);
			var inputY = guiTop + (restocker ? 12 : 28) + i / 3 * 20;
			if (mouseX < inputX || mouseX >= inputX + 16 || mouseY < inputY || mouseY >= inputY + 16) continue;
			var itemStack = inputConfig.get(i);
			if (itemStack.stack.isEmpty()) return false;
			var max = CCG.config.misc.removeRequestLimit ? Integer.MAX_VALUE : 64;
			ccg$popup.open(itemStack.stack, itemStack.count, max, count -> itemStack.count = count);
			return true;
		}
		return false;
	}
	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
	private void mouseScrolledEarlyReturn(
		double mouseX,
		double mouseY,
		double scrollX,
		double scrollY,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (ccg$popup.open) cir.setReturnValue(true);
	}
	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (!ccg$popup.open) return super.charTyped(codePoint, modifiers);
		return ccg$popup.charTyped(codePoint, modifiers);
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!ccg$popup.open) return super.keyPressed(keyCode, scanCode, modifiers);
		return ccg$popup.keyPressed(keyCode, scanCode, modifiers);
	}
}
