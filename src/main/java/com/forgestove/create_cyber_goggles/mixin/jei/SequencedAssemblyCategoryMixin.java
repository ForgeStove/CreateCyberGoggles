package com.forgestove.create_cyber_goggles.mixin.jei;
import com.forgestove.create_cyber_goggles.*;
import com.simibubi.create.compat.jei.category.SequencedAssemblyCategory;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.CreateLang;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(SequencedAssemblyCategory.class)
public abstract class SequencedAssemblyCategoryMixin {
	@Inject(method = "setRecipe*", at = @At("TAIL"), remap = false)
	private void setRecipe(IRecipeLayoutBuilder builder, SequencedAssemblyRecipe recipe, IFocusGroup focuses, CallbackInfo callbackInfo) {
		if (!CreateCyberGoggles.config.jei.nonrandomScrap) return;
		var size = 8;
		for (var i = 1; i < recipe.resultPool.size(); i++) {
			var out = recipe.resultPool.get(i);
			builder.addSlot(RecipeIngredientRole.OUTPUT, (i - 1) % size * 19 + 15, (i - 1) / size * 19 + 120)
				   .setBackground(Util.asDrawable(AllGuiTextures.JEI_CHANCE_SLOT), -1, -1).addItemStack(out.getStack())
				   .addRichTooltipCallback((iRecipeSlotView, iTooltipBuilder) -> {
					   float totalWeight = 0;
					   for (var output : recipe.resultPool) totalWeight += output.getChance();
					   iTooltipBuilder.add(chanceComponent(out.getChance() / totalWeight));
				   });
		}
	}
	@Shadow(remap = false)
	protected abstract MutableComponent chanceComponent(float chance);
	@Inject(method = "chanceComponent", at = @At("HEAD"), remap = false, cancellable = true)
	protected void chanceComponent(float chance, CallbackInfoReturnable<MutableComponent> returnable) {
		if (!CreateCyberGoggles.config.goggles.preciseNumbers) return;
		if (chance * 100 == (int) (chance * 100)) return;
		returnable.setReturnValue(CreateLang.translateDirect("recipe.processing.chance", chance * 100).withStyle(ChatFormatting.GOLD));
	}
}
