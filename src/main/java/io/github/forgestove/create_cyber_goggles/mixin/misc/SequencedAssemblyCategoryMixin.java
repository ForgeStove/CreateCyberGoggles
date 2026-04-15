package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.compat.jei.category.*;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
import static io.github.forgestove.create_cyber_goggles.core.util.SequencedAssemblyUtil.*;
@Mixin(SequencedAssemblyCategory.class)
public abstract class SequencedAssemblyCategoryMixin extends CreateRecipeCategory<SequencedAssemblyRecipe> {
	protected SequencedAssemblyCategoryMixin(Info<SequencedAssemblyRecipe> info) {
		super(info);
	}
	@Shadow
	protected abstract MutableComponent chanceComponent(float chance);
	@Override
	public void createRecipeExtras(
		@NotNull IRecipeExtrasBuilder builder,
		@NotNull RecipeHolder<SequencedAssemblyRecipe> holder,
		@NotNull IFocusGroup focuses
	) {
		if (!CCG.config.misc.showScrapContent) return;
		var recipe = holder.value();
		if (!shouldEnable(recipe)) return;
		builder.addInputHandler(createInputHandler(recipe));
	}
	@Inject(method = "draw*", at = @At("TAIL"))
	public void draw(
		SequencedAssemblyRecipe recipe,
		IRecipeSlotsView iRecipeSlotsView,
		GuiGraphics gui,
		double mouseX,
		double mouseY,
		CallbackInfo ci
	) {
		if (!CCG.config.misc.showScrapContent || recipe.getOutputChance() == 1) return;
		var junkCount = getJunkCount(recipe);
		if (junkCount <= 0) return;
		var state = getState(recipe);
		state[0] = Math.floorMod((int) state[0], junkCount);
		if (mc.level == null) return;
		var now = System.currentTimeMillis();
		if (state[1] == 0) state[1] = now;
		var hoveringJunk = isOverJunkSlot(mouseX, mouseY);
		if (!hoveringJunk && now - state[1] >= AUTO_ROTATE_INTERVAL_MS) {
			state[0] = ((int) state[0] + 1) % junkCount;
			state[1] = now;
		}
		var selected = recipe.resultPool.get((int) state[0] + 1).getStack();
		// Redraw slot background first so Create's default '?' marker is covered.
		AllGuiTextures.JEI_CHANCE_SLOT.render(gui, JUNK_X, JUNK_Y);
		gui.renderItem(selected, JUNK_X + 1, JUNK_Y + 1);
	}
	@Inject(method = "getTooltipStrings*", at = @At("HEAD"), cancellable = true)
	public void getTooltipStrings(
		SequencedAssemblyRecipe recipe,
		IRecipeSlotsView iRecipeSlotsView,
		double mouseX,
		double mouseY,
		CallbackInfoReturnable<List<Component>> cir
	) {
		if (!CCG.config.misc.showScrapContent || recipe.getOutputChance() == 1 || !isOverJunkSlot(mouseX, mouseY)) return;
		var junkCount = getJunkCount(recipe);
		if (junkCount <= 0) return;
		var state = getState(recipe);
		state[0] = Math.floorMod((int) state[0], junkCount);
		float totalWeight = 0;
		for (var i = 1; i < recipe.resultPool.size(); i++) totalWeight += recipe.resultPool.get(i).getChance();
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(CreateLang.translateDirect("recipe.assembly.junk"));
		tooltip.add(chanceComponent(1 - recipe.getOutputChance()));
		CCGLang.translate(ChatFormatting.DARK_GRAY, "tooltip.sequenced_assembly.scroll_cycle").addTo(tooltip);
		for (var i = 0; i < junkCount; i++) {
			var out = recipe.resultPool.get(i + 1);
			var line = Component.literal(i == (int) state[0] ? "> " : "  ")
				.append(out.getStack().getHoverName().copy().withStyle(i == (int) state[0] ? ChatFormatting.GREEN : ChatFormatting.GRAY));
			if (totalWeight > 0) {
				line.append(Component.literal(" "));
				line.append(chanceComponent(out.getChance() / totalWeight));
			}
			tooltip.add(line);
		}
		var selected = recipe.resultPool.get((int) state[0] + 1).getStack();
		tooltip.addAll(selected.getTooltipLines(
			TooltipContext.EMPTY,
			mc.player,
			mc.options.advancedItemTooltips ? Default.ADVANCED : Default.NORMAL
		));
		cir.setReturnValue(tooltip);
	}
}
