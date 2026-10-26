package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.redstone.thresholdSwitch.*;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour.StepContext;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.event.CCGKey;
import io.github.forgestove.create_cyber_goggles.factory.*;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.function.Function;

import static io.github.forgestove.create_cyber_goggles.util.CCGUtil.*;
@Mixin(ThresholdSwitchScreen.class)
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
	private static int ccg$scaleLevel(int value, double factor, int min, int max, int step) {
		int scaled = MultiplyScreen.scale(value, factor, min, max);
		return Mth.clamp(Math.round((float) scaled / step) * step, min, max);
	}
	@Shadow
	protected abstract int getValueStep();
	@Unique
	private void ccg$openPopup(ScrollInput input, boolean isOnAbove) {
		int valueStep = Math.max(1, getValueStep());
		int max = (isOnAbove ? blockEntity.currentMaxLevel : blockEntity.currentMaxLevel - valueStep) / valueStep;
		mc.setScreen(new RequestAmountScreen(
			this,
			new ItemStack(AllBlocks.THRESHOLD_SWITCH.get()),
			input.getState() / valueStep,
			Math.max(0, max),
			count -> ccg$apply(input, count, valueStep, isOnAbove)
		));
	}
	@Shadow
	protected abstract void send(boolean invert);
	@Unique
	private void ccg$apply(ScrollInput input, int count, int valueStep, boolean isOnAbove) {
		int state = isOnAbove
			? Mth.clamp(count * valueStep, blockEntity.currentMinLevel + valueStep, blockEntity.currentMaxLevel)
			: Mth.clamp(count * valueStep, blockEntity.currentMinLevel, blockEntity.currentMaxLevel - valueStep);
		input.setState(state);
		input.onChanged();
		send(blockEntity.isInverted());
	}
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
			int amount = getModifiedScrollAmount();
			int valueStep = getValueStep();
			if (sc.currentValue / valueStep == 1 && amount > 1) amount--;
			return amount * valueStep;
		};
		return original.call(instance, modified);
	}
	@Inject(method = "init", at = @At("TAIL"))
	private void ccg$addMultiplier(CallbackInfo ci) {
		if (!CCG.config.misc.multiplyAllValues) return;
		addRenderableWidget(MultiplyScreen.createButton(guiLeft + 35, guiTop + 79, this::ccg$multiplyAll));
	}
	/** 覆写 Shift 滚动逻辑 */
	@Inject(method = "updateInputBoxes", at = @At("TAIL"))
	private void ccg$alignShiftStep(CallbackInfo ci) {
		if (!CCG.config.misc.quickRequestActions) return;
		int valueStep = getValueStep();
		onAbove.withShiftStep(valueStep);
		offBelow.withShiftStep(valueStep);
	}
	@Unique
	private void ccg$multiplyAll(double factor) {
		int valueStep = Math.max(1, getValueStep());
		int min = blockEntity.currentMinLevel;
		int max = blockEntity.currentMaxLevel;
		int off = ccg$scaleLevel(offBelow.getState(), factor, min, max - valueStep, valueStep);
		int on = ccg$scaleLevel(onAbove.getState(), factor, min + valueStep, max, valueStep);
		if (on < off + valueStep) on = Math.min(max, off + valueStep);
		offBelow.setState(Math.min(off, on - valueStep));
		onAbove.setState(on);
		offBelow.onChanged();
		onAbove.onChanged();
		send(blockEntity.isInverted());
	}
}
