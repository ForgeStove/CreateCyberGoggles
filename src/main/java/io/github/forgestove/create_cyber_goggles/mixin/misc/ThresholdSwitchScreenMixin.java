package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.redstone.thresholdSwitch.*;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import io.github.forgestove.create_cyber_goggles.core.factory.RequestAmountScreen;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
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
		input.onChanged(); // 触发原 calling 自动联动
		send(blockEntity.isInverted());
	}
	@Shadow
	protected abstract void send(boolean invert);
}
