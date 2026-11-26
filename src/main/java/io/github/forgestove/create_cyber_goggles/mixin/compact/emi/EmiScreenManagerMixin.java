package io.github.forgestove.create_cyber_goggles.mixin.compact.emi;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext.*;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.screen.EmiScreenManager;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Pseudo
@Mixin(EmiScreenManager.class)
public abstract class EmiScreenManagerMixin {
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private static void ccg$altFillRedstoneRequester(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.misc.jei.redstoneRequesterJEIRequest || button != 0 || !Screen.hasAltDown()) return;
		AbstractContainerScreen<?> base = EmiApi.getHandledScreen();
		if (!(base instanceof RedstoneRequesterScreen screen)) return;
		EmiStackInteraction hovered = EmiScreenManager.getHoveredStack((int) mouseX, (int) mouseY, false);
		EmiRecipe recipe = hovered.getRecipeContext();
		if (recipe == null) return;
		int amount = Screen.hasShiftDown() ? Integer.MAX_VALUE : 1;
		if (EmiRecipeFiller.performFill(recipe, screen, Type.CRAFTABLE, Destination.NONE, amount)) cir.setReturnValue(true);
	}
}
