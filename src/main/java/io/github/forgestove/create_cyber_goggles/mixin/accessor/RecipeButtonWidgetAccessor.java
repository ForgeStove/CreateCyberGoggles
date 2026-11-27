package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.widget.RecipeButtonWidget;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
@Pseudo
@Mixin(RecipeButtonWidget.class)
public interface RecipeButtonWidgetAccessor {
	@Accessor
	EmiRecipe getRecipe();
}
