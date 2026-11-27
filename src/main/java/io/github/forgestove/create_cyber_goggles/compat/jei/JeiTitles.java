package io.github.forgestove.create_cyber_goggles.compat.jei;
import com.simibubi.create.compat.jei.CreateJEI;
import io.github.forgestove.create_cyber_goggles.foundation.util.Mods;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.*;
public class JeiTitles {
	private static final Map<String, Component> TITLES = new HashMap<>();
	private static boolean built;
	public static Component get(String id) {
		if (!built) Mods.jei.executeIfInstalled(JeiTitles::collectTitles);
		return TITLES.get(id);
	}
	private static void collectTitles() {
		if (built) return;
		IJeiRuntime runtime = CreateJEI.runtime;
		if (runtime == null) return;
		built = true;
		IRecipeManager manager = runtime.getRecipeManager();
		List<IRecipeCategory<?>> categories = manager.createRecipeCategoryLookup().includeHidden().get().toList();
		for (IRecipeCategory<?> category : categories)
			TITLES.putIfAbsent(category.getRecipeType().getUid().toString(), category.getTitle());
		for (IRecipeCategory<?> category : categories) {
			RecipeType<?> type = category.getRecipeType();
			manager.createRecipeLookup(type).get().findFirst().ifPresent(recipe -> {
				if (!(recipe instanceof RecipeHolder<?> holder)) return;
				ResourceLocation id = BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType());
				if (id != null) TITLES.putIfAbsent(id.toString(), category.getTitle());
			});
		}
	}
}
