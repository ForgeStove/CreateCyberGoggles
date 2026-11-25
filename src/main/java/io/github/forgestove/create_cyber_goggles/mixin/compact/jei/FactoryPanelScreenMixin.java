package io.github.forgestove.create_cyber_goggles.mixin.compact.jei;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.foundation.lang.CCGLang;
import io.github.forgestove.create_cyber_goggles.foundation.util.CCGMods;
import io.github.forgestove.create_cyber_goggles.mixin.ModCondition;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
@ModCondition(conflict = CCGMods.extra_gauges)
@Mixin(FactoryPanelScreen.class)
public abstract class FactoryPanelScreenMixin extends AbstractSimiScreen {
	@Shadow private boolean craftingActive;
	@Shadow private CraftingRecipe availableCraftingRecipe;
	@Shadow private List<BigItemStack> craftingIngredients;
	@WrapOperation(
		method = "searchForCraftingRecipe", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/world/item/crafting/RecipeManager;getAllRecipesFor(Lnet/minecraft/world/item/crafting/RecipeType;)"
			+ "Ljava/util/List;"
	)
	)
	private List<RecipeHolder<Recipe<RecipeInput>>> includeMechanicalCrafting(
		RecipeManager instance,
		RecipeType<Recipe<RecipeInput>> type,
		Operation<List<RecipeHolder<Recipe<RecipeInput>>>> original
	) {
		if (!CCG.config.misc.jei.allowLargeCrafting) return original.call(instance, type);
		var recipes = new ArrayList<>(original.call(instance, type));
		recipes.addAll(instance.getAllRecipesFor(AllRecipeTypes.MECHANICAL_CRAFTING.getType()));
		return recipes;
	}
	@Inject(method = "renderInputItem", at = @At("HEAD"), cancellable = true)
	private void renderLargeTotals(GuiGraphics gui, int slot, BigItemStack itemStack, int mouseX, int mouseY, CallbackInfo ci) {
		if (!CCG.config.misc.jei.allowLargeCrafting) return;
		if (!(craftingActive && availableCraftingRecipe instanceof ShapedRecipe shaped && shaped.getWidth() > 3)) return;
		List<BigItemStack> totals = ccg$totals();
		if (slot >= totals.size()) {
			ci.cancel();
			return;
		}
		BigItemStack total = totals.get(slot);
		int inputX = guiLeft + 68 + slot % 3 * 20;
		int inputY = guiTop + 28 + slot / 3 * 20;
		gui.renderItem(total.stack, inputX, inputY);
		gui.renderItemDecorations(font, total.stack, inputX, inputY, total.count + "");
		if (slot == 0 && mouseX >= inputX - 2 && mouseX < inputX - 2 + 60 && mouseY >= inputY - 2 && mouseY < inputY - 2 + 60) {
			List<ItemStack> recipeItems = craftingIngredients.stream().map(b -> b.stack).toList();
			List<Component> tooltip = new ArrayList<>();
			CCGLang.of().itemList(recipeItems, shaped.getWidth()).addTo(tooltip);
			gui.renderComponentTooltip(font, tooltip, mouseX, mouseY);
		}
		ci.cancel();
	}
	@Unique
	private List<BigItemStack> ccg$totals() {
		List<BigItemStack> totals = new ArrayList<>();
		for (BigItemStack entry : craftingIngredients) {
			if (entry.stack.isEmpty()) continue;
			BigItemStack existing = null;
			for (BigItemStack t : totals) {
				if (!ItemStack.isSameItemSameComponents(t.stack, entry.stack)) continue;
				existing = t;
				break;
			}
			if (existing != null) existing.count++;
			else totals.add(new BigItemStack(entry.stack.copyWithCount(1), 1));
		}
		return totals;
	}
}
