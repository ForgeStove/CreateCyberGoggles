package com.forgestove.create_cyber_goggles.mixin.jei;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.simibubi.create.compat.jei.category.SequencedAssemblyCategory;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(SequencedAssemblyCategory.class)
public abstract class SequencedAssemblyCategoryMixin {
	@Inject(method = "setRecipe*", at = @At("TAIL"), remap = false)
	private void setRecipe(IRecipeLayoutBuilder builder, SequencedAssemblyRecipe recipe, IFocusGroup focuses, CallbackInfo callbackInfo) {
		if (!CCGConfig.get().jei.nonrandomScrap) return;
		var size = 8;
		for (var i = 1; i < recipe.resultPool.size(); i++) {
			var out = recipe.resultPool.get(i);
			builder.addSlot(RecipeIngredientRole.OUTPUT, (i - 1) % size * 19 + 15, (i - 1) / size * 19 + 120).setBackground(
				new IDrawable() {
					public int getWidth() {return AllGuiTextures.JEI_CHANCE_SLOT.width;}
					public int getHeight() {return AllGuiTextures.JEI_CHANCE_SLOT.height;}
					public void draw(@NotNull GuiGraphics guiGraphics, int xOffset, int yOffset) {
						AllGuiTextures.JEI_CHANCE_SLOT.render(guiGraphics, xOffset, yOffset);
					}
				}, -1, -1
			).addItemStack(out.getStack()).addRichTooltipCallback((iRecipeSlotView, iTooltipBuilder) -> {
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
		if (!CCGConfig.get().goggles.preciseNumbers) return;
		if (chance * 100 == (int) (chance * 100)) return;
		returnable.setReturnValue(Lang.translateDirect("recipe.processing.chance", chance * 100).withStyle(ChatFormatting.GOLD));
	}
}
