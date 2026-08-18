package io.github.forgestove.create_cyber_goggles.mixin.compact.emi;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import dev.emi.emi.jemi.JemiRecipe;
import dev.emi.emi.screen.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.SequencedAssemblyUtil.*;
@Pseudo
@Mixin(RecipeScreen.class)
public abstract class RecipeScreenMixin {
	@Shadow private List<WidgetGroup> currentPage;
	// EMI 的 Widget 没有 mouseScrolled，滚轮只在 RecipeScreen 层处理；JEMI 又丢弃了
	// IRecipeExtrasBuilder 里的 inputHandlers，因此副产物滚轮切换需在此拦截。
	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
	private void ccg$mouseScrolled(double mouseX, double mouseY, double horizontal, double amount, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.misc.jei.showScrapContent || amount == 0) return;
		for (var group : currentPage) {
			if (!(group.recipe instanceof JemiRecipe<?> jemi)) continue;
			if (!(jemi.recipe instanceof RecipeHolder<?> holder)) continue;
			if (!(holder.value() instanceof SequencedAssemblyRecipe recipe)) continue;
			if (recipe.getOutputChance() == 1) continue;
			if (!isOverJunkSlot(mouseX - group.x, mouseY - group.y)) continue;
			if (!scrollJunk(recipe, amount)) continue;
			cir.setReturnValue(true);
			return;
		}
	}
}
