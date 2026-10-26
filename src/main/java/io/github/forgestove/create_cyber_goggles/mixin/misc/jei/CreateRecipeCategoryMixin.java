package io.github.forgestove.create_cyber_goggles.mixin.misc.jei;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
import static io.github.forgestove.create_cyber_goggles.core.util.SequencedAssemblyUtil.*;
import static net.minecraft.network.chat.Component.literal;
@Mixin(CreateRecipeCategory.class)
public abstract class CreateRecipeCategoryMixin<T extends Recipe<?>> implements IRecipeCategory<RecipeHolder<T>> {
	@Unique
	private static MutableComponent ccg$chanceComponent(float chance) {
		String number = chance * 100 % 1 == 0 ? String.valueOf((int) (chance * 100)) : String.format("%.2f", chance * 100);
		return CCGCreateLang.translateDirect("recipe.processing.chance", number).withStyle(ChatFormatting.GOLD);
	}
	@Inject(
		method = "getTooltipStrings(Lnet/minecraft/world/item/crafting/RecipeHolder;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;DD)"
			+ "Ljava/util/List;", at = @At("HEAD"), cancellable = true
	)
	public void getTooltipStrings(
		RecipeHolder<T> holder,
		IRecipeSlotsView recipeSlotsView,
		double mouseX,
		double mouseY,
		CallbackInfoReturnable<List<Component>> cir
	) {
		T t = holder.value();
		if (!(t instanceof SequencedAssemblyRecipe recipe)) return;
		if (!CCG.config.misc.jei.showScrapContent || recipe.getOutputChance() == 1 || !isOverJunkSlot(mouseX, mouseY)) return;
		int junkCount = getJunkCount(recipe);
		if (junkCount <= 0) return;
		long[] state = getState(recipe);
		state[0] = Math.floorMod((int) state[0], junkCount);
		float totalWeight = 0;
		for (var i = 1; i < recipe.resultPool.size(); i++) totalWeight += recipe.resultPool.get(i).getChance();
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(CCGCreateLang.translateDirect("recipe.assembly.junk"));
		tooltip.add(ccg$chanceComponent(1 - recipe.getOutputChance()));
		tooltip.add(CCGLang.translate("tooltip.sequencedAssembly.scrollCycle", ChatFormatting.DARK_GRAY).component());
		for (var i = 0; i < junkCount; i++) {
			ProcessingOutput out = recipe.resultPool.get(i + 1);
			MutableComponent line = literal(i == (int) state[0] ? "> " : "  ").append(out.getStack()
				.getHoverName()
				.copy()
				.withStyle(i == (int) state[0] ? ChatFormatting.GREEN : ChatFormatting.GRAY));
			if (totalWeight > 0) {
				line.append(literal(" "));
				line.append(ccg$chanceComponent(out.getChance() / totalWeight));
			}
			tooltip.add(line);
		}
		ItemStack selected = recipe.resultPool.get((int) state[0] + 1).getStack();
		tooltip.addAll(selected.getTooltipLines(
			TooltipContext.EMPTY,
			mc.player,
			mc.options.advancedItemTooltips ? Default.ADVANCED : Default.NORMAL
		));
		cir.setReturnValue(tooltip);
	}
	@Inject(
		method = "draw(Lnet/minecraft/world/item/crafting/RecipeHolder;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;"
			+ "Lnet/minecraft/client/gui/GuiGraphics;DD)V", at = @At("TAIL")
	)
	public void draw(
		RecipeHolder<T> holder,
		IRecipeSlotsView recipeSlotsView,
		GuiGraphics gui,
		double mouseX,
		double mouseY,
		CallbackInfo ci
	) {
		T t = holder.value();
		if (!(t instanceof SequencedAssemblyRecipe recipe)) return;
		if (!CCG.config.misc.jei.showScrapContent || recipe.getOutputChance() == 1) return;
		int junkCount = getJunkCount(recipe);
		if (junkCount <= 0) return;
		long[] state = getState(recipe);
		state[0] = Math.floorMod((int) state[0], junkCount);
		if (mc.level == null) return;
		long now = System.currentTimeMillis();
		if (state[1] == 0) state[1] = now;
		boolean hoveringJunk = isOverJunkSlot(mouseX, mouseY);
		if (!hoveringJunk && now - state[1] >= AUTO_ROTATE_INTERVAL_MS) {
			state[0] = ((int) state[0] + 1) % junkCount;
			state[1] = now;
		}
		ItemStack selected = recipe.resultPool.get((int) state[0] + 1).getStack();
		// 先重新绘制槽位背景，以覆盖 Create 默认的 '?' 标记。
		AllGuiTextures.JEI_CHANCE_SLOT.render(gui, JUNK_X, JUNK_Y);
		gui.renderItem(selected, JUNK_X + 1, JUNK_Y + 1);
	}
	@Override
	public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder, @NotNull RecipeHolder<T> holder, @NotNull IFocusGroup focuses) {
		IRecipeCategory.super.createRecipeExtras(builder, holder, focuses);
		if (!CCG.config.misc.jei.showScrapContent) return;
		T t = holder.value();
		if (!(t instanceof SequencedAssemblyRecipe recipe)) return;
		if (!shouldEnable(recipe)) return;
		builder.addInputHandler(createInputHandler(recipe));
	}
}
