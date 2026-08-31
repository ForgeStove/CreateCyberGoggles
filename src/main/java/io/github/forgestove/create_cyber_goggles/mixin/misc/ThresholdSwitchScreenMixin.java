package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.redstone.thresholdSwitch.*;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour.StepContext;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import io.github.forgestove.create_cyber_goggles.core.factory.RequestAmountScreen;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.function.Function;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(value = ThresholdSwitchScreen.class, remap = false)
public abstract class ThresholdSwitchScreenMixin extends AbstractSimiScreen {
	@Shadow private ScrollInput offBelow;
	@Shadow private ScrollInput onAbove;
	@Shadow private ThresholdSwitchBlockEntity blockEntity;
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void openPopup(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (button != InputConstants.MOUSE_BUTTON_MIDDLE || !CCG.config.misc.quickRequestActions || !CCGKey.stockRequestSetter.isDown())
			return;
		if (ccg$openPopupForHoveredInput(mouseX, mouseY)) cir.setReturnValue(true);
	}
	@Unique
	private boolean ccg$openPopupForHoveredInput(double mouseX, double mouseY) {
		if (onAbove.visible && onAbove.isMouseOver(mouseX, mouseY)) {
			ccg$openPopup(onAbove, true);
			return true;
		}
		if (offBelow.visible && offBelow.isMouseOver(mouseX, mouseY)) {
			ccg$openPopup(offBelow, false);
			return true;
		}
		return false;
	}
	@Unique
	private void ccg$openPopup(ScrollInput input, boolean isOnAbove) {
		var valueStep = Math.max(1, getValueStep());
		var max = (isOnAbove ? blockEntity.currentMaxLevel : blockEntity.currentMaxLevel - valueStep) / valueStep;
		mc.setScreen(new RequestAmountScreen(
			this,
			new ItemStack(AllBlocks.THRESHOLD_SWITCH.get()),
			input.getState() / valueStep,
			Math.max(0, max),
			count -> ccg$apply(input, count, valueStep, isOnAbove)
		));
	}
	@Shadow
	protected abstract int getValueStep();
	@Unique
	private void ccg$apply(ScrollInput input, int count, int valueStep, boolean isOnAbove) {
		var state = isOnAbove
			? Mth.clamp(count * valueStep, blockEntity.currentMinLevel + valueStep, blockEntity.currentMaxLevel)
			: Mth.clamp(count * valueStep, blockEntity.currentMinLevel, blockEntity.currentMaxLevel - valueStep);
		input.setState(state);
		input.onChanged();
		send(blockEntity.isInverted());
	}
	@Shadow
	protected abstract void send(boolean invert);
	@WrapOperation(
		method = "init", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/foundation/gui/widget/ScrollInput;withStepFunction(Ljava/util/function/Function;)"
			+ "Lcom/simibubi/create/foundation/gui/widget/ScrollInput;"
	)
	)
	private ScrollInput ccg$modifyScrollStep(ScrollInput instance, Function<StepContext, Integer> step, Operation<ScrollInput> original) {
		if (!CCG.config.misc.quickRequestActions) return original.call(instance, step);
		Function<StepContext, Integer> modified = sc -> {
			var amount = getModifiedScrollAmount();
			var valueStep = getValueStep();
			if (sc.currentValue / valueStep == 1 && amount > 1) amount--;
			return amount * valueStep;
		};
		return original.call(instance, modified);
	}
	/** 覆写 Shift 滚动逻辑 */
	@Inject(method = "updateInputBoxes", at = @At("TAIL"))
	private void ccg$alignShiftStep(CallbackInfo ci) {
		if (!CCG.config.misc.quickRequestActions) return;
		var valueStep = getValueStep();
		onAbove.withShiftStep(valueStep);
		offBelow.withShiftStep(valueStep);
	}
}
