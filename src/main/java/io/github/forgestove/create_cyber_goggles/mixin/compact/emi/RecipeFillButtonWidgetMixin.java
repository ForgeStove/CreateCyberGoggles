package io.github.forgestove.create_cyber_goggles.mixin.compact.emi;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.widget.RecipeFillButtonWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
/**
 * 为 EMI 配方转移按钮（「+」）追加「按住 Alt 以原版方式填入」提示，与 JEI 侧
 * {@code RecipeTransferButtonControllerMixin} 对应。转移逻辑经 JemiRecipeHandler
 * 桥接到 RedstoneRequesterTransferHandler，Alt 切换在原版/动力合成器方式间已生效。
 */
@Pseudo
@Mixin(RecipeFillButtonWidget.class)
public abstract class RecipeFillButtonWidgetMixin {
	@Inject(method = "getTooltip", at = @At("TAIL"), cancellable = true)
	private void ccg$redstoneRequesterHint(int mouseX, int mouseY, CallbackInfoReturnable<List<ClientTooltipComponent>> cir) {
		var screen = EmiApi.getHandledScreen();
		if (screen == null || !(screen.getMenu() instanceof RedstoneRequesterMenu)) return;
		var tooltip = new ArrayList<>(cir.getReturnValue());
		tooltip.add(ClientTooltipComponent.create(EmiPort.ordered(Component.translatable(
			"create_cyber_goggles.gui.redstoneRequester.jeiAltHint").withStyle(ChatFormatting.DARK_GRAY))));
		cir.setReturnValue(tooltip);
	}
}
