package io.github.forgestove.create_cyber_goggles.mixin.misc.jei;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.factory.CCGMods;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
@Mixin(FactoryPanelScreen.class)
public abstract class FactoryPanelScreenMixin extends AbstractSimiScreen {
	@Shadow private boolean craftingActive;
	@Shadow private CraftingRecipe availableCraftingRecipe;
	@Shadow private List<BigItemStack> craftingIngredients;
	/**
	 * "使用动力合成"按钮只会在找到配方时显示；searchForCraftingRecipe 只搜 RecipeType.CRAFTING，
	 * 动力合成配方（MECHANICAL_CRAFTING 类型）不会被识别。此处合并两类配方，使非 3x3 配方也能显示按钮。
	 */
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
		if (!CCG.config.misc.jei.allowLargeCrafting || CCGMods.extra_gauges.isLoaded()) return original.call(instance, type);
		var recipes = new ArrayList<>(original.call(instance, type));
		recipes.addAll(instance.getAllRecipesFor(AllRecipeTypes.MECHANICAL_CRAFTING.getType()));
		return recipes;
	}
	/** >3×3 配方改为在 3×3 区域内显示总计原料（避免完整网格出界）；悬停 3×3 框显示实际配方样式 */
	@Inject(method = "renderInputItem", at = @At("HEAD"), cancellable = true)
	private void renderLargeTotals(GuiGraphics graphics, int slot, BigItemStack itemStack, int mouseX, int mouseY, CallbackInfo ci) {
		if (!CCG.config.misc.jei.allowLargeCrafting || CCGMods.extra_gauges.isLoaded()) return;
		if (!(craftingActive && availableCraftingRecipe instanceof ShapedRecipe shaped && shaped.getWidth() > 3)) return;
		var totals = ccg$totals();
		if (slot >= totals.size()) {
			ci.cancel();
			return;
		}
		var total = totals.get(slot);
		var inputX = guiLeft + 68 + slot % 3 * 20;
		var inputY = guiTop + 28 + slot / 3 * 20;
		graphics.renderItem(total.stack, inputX, inputY);
		graphics.renderItemDecorations(font, total.stack, inputX, inputY, total.count + "");
		if (slot == 0 && mouseX >= inputX - 2 && mouseX < inputX - 2 + 60 && mouseY >= inputY - 2 && mouseY < inputY - 2 + 60) {
			var recipeItems = craftingIngredients.stream().map(b -> b.stack).toList();
			List<Component> tooltip = new ArrayList<>();
			CCGLang.itemList(recipeItems, shaped.getWidth()).addTo(tooltip);
			graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
		}
		ci.cancel();
	}
	/** >3×3 配方把原料按种类汇总（最多 9 格） */
	@Unique
	private List<BigItemStack> ccg$totals() {
		List<BigItemStack> totals = new ArrayList<>();
		for (var entry : craftingIngredients) {
			if (entry.stack.isEmpty()) continue;
			BigItemStack existing = null;
			for (var t : totals) {
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
