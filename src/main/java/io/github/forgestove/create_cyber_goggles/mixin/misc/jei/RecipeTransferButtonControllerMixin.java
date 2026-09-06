package io.github.forgestove.create_cyber_goggles.mixin.misc.jei;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.gui.recipes.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/**
 * 在 JEI 配方转移按钮的悬停提示里，为红石请求器追加「按住 Alt 以原版方式填入」说明。
 */
@Pseudo
@Mixin(RecipeTransferButtonController.class)
public abstract class RecipeTransferButtonControllerMixin {
	@Final @Shadow private RecipesGui recipesGui;
	@Inject(method = "getTooltips", at = @At("TAIL"))
	public void redstoneRequesterHint(ITooltipBuilder tooltip, CallbackInfo ci) {
		if (recipesGui.getParentContainerMenu() instanceof RedstoneRequesterMenu)
			tooltip.add(Component.translatable("create_cyber_goggles.gui.redstoneRequester.jeiAltHint")
				.withStyle(ChatFormatting.DARK_GRAY));
	}
}
