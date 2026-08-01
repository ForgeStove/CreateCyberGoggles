package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
@Mixin(FactoryPanelScreen.class)
public abstract class FactoryPanelScreenMixin extends AbstractSimiScreen {
	@Shadow private boolean restocker;
	@Shadow private IconButton relocateButton;
	@Shadow private boolean craftingActive;
	@Shadow private CraftingRecipe availableCraftingRecipe;
	@ModifyConstant(method = "mouseScrolled", constant = @Constant(intValue = 64))
	public int modifyMaxScrollAmount(int original) {
		return CCG.config.misc.removeRequestLimit ? Integer.MAX_VALUE : original;
	}
	@ModifyConstant(method = "mouseScrolled", constant = @Constant(intValue = 10))
	public int modifyPerScrollAmount(int original, @Local(name = "itemStack") BigItemStack itemStack) {
		return CCG.config.misc.removeRequestLimit ? Item.DEFAULT_MAX_STACK_SIZE - (itemStack.count == 1 ? 1 : 0) : original;
	}
	@Inject(method = "init", at = @At("TAIL"))
	public void init(CallbackInfo ci) {
		if (!CCG.config.goggles.betterFactoryGauge) return;
		if (!restocker) return;
		relocateButton.setPosition(relocateButton.getX() - 23, relocateButton.getY() - 54);
		addRenderableWidget(relocateButton);
	}
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
		if (!CCG.config.misc.allowLargeCrafting) return original.call(instance, type);
		var recipes = new ArrayList<>(original.call(instance, type));
		recipes.addAll(instance.getAllRecipesFor(AllRecipeTypes.MECHANICAL_CRAFTING.getType()));
		return recipes;
	}
	/** renderInputItem 原固定按 3×3 网格布局（slot % 3 / slot / 3），改为按配方实际宽度动态布局 */
	@ModifyConstant(method = "renderInputItem", constant = @Constant(intValue = 3, ordinal = 0))
	private int gridColumnsInX(int value) {
		return CCG.config.misc.allowLargeCrafting ? ccg$gridColumns() : value;
	}
	@Unique
	private int ccg$gridColumns() {
		if (craftingActive && availableCraftingRecipe instanceof ShapedRecipe shaped) return Math.max(3, shaped.getWidth());
		return 3;
	}
	@ModifyConstant(method = "renderInputItem", constant = @Constant(intValue = 3, ordinal = 1))
	private int gridColumnsInY(int value) {
		return CCG.config.misc.allowLargeCrafting ? ccg$gridColumns() : value;
	}
}
