package io.github.forgestove.create_cyber_goggles.mixin.compact.emi;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.handler.EmiCraftContext.Destination;
import dev.emi.emi.api.recipe.handler.EmiCraftContext.Type;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/**
 * EMI 侧栏点击填入的 Alt 修饰键被 {@link dev.emi.emi.input.EmiBind} 的精确修饰匹配拦截
 * （按住 Alt 时 craftOne/craftAll 绑定不匹配，左击落入打开配方页）。此处在红石请求器
 * 界面上，按住 Alt 左键点击侧栏书签时强制触发填入；转移逻辑经 JemiRecipeHandler 桥接到
 * RedstoneRequesterTransferHandler，其内部读 {@link Screen#hasAltDown()} 自动切原版网格方式。
 * <p>只 hook {@code mouseClicked}（不 hook 服务键盘的 stackInteraction）：Alt 键本身落下
 * 也走 stackInteraction，若在此处拦截会"悬停+按 Alt 即填入"，故仅对真正的鼠标点击生效。
 */
@Pseudo
@Mixin(EmiScreenManager.class)
public abstract class EmiScreenManagerMixin {
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private static void ccg$altFillRedstoneRequester(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (button != 0 || !Screen.hasAltDown()) return;
		var base = EmiApi.getHandledScreen();
		if (!(base instanceof RedstoneRequesterScreen screen)) return;
		var hovered = EmiScreenManager.getHoveredStack((int) mouseX, (int) mouseY, false);
		var recipe = hovered.getRecipeContext();
		if (recipe == null) return;
		// 以原版方式填入：performFill 桥接的 transferRecipe 读 hasAltDown()，无需手动区分
		if (EmiRecipeFiller.performFill(recipe, screen, Type.CRAFTABLE, Destination.NONE, 1)) cir.setReturnValue(true);
	}
}
