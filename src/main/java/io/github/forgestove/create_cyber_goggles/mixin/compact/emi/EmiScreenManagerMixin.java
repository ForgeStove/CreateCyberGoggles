package io.github.forgestove.create_cyber_goggles.mixin.compact.emi;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.handler.EmiCraftContext.*;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.screen.EmiScreenManager;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/**
 * EMI 的侧栏点击按绑定精确匹配修饰键，按住 Alt 时 craft 绑定落空、左击会改为打开配方页；
 * <p>这里在红石请求器界面上强制触发填入。
 */
@Pseudo
@Mixin(EmiScreenManager.class)
public abstract class EmiScreenManagerMixin {
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private static void ccg$altFillRedstoneRequester(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.misc.jei.redstoneRequesterJEIRequest || button != 0 || !Screen.hasAltDown()) return;
		var base = EmiApi.getHandledScreen();
		if (!(base instanceof RedstoneRequesterScreen screen)) return;
		var hovered = EmiScreenManager.getHoveredStack((int) mouseX, (int) mouseY, false);
		var recipe = hovered.getRecipeContext();
		if (recipe == null) return;
		// Shift 对应 EMI 的 craftAll 绑定
		var amount = Screen.hasShiftDown() ? Integer.MAX_VALUE : 1;
		if (EmiRecipeFiller.performFill(recipe, screen, Type.CRAFTABLE, Destination.NONE, amount)) cir.setReturnValue(true);
	}
}
