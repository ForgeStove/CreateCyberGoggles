package io.github.forgestove.create_cyber_goggles.compat.jei;
import com.simibubi.create.compat.jei.CreateJEI;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.ModList;

import java.util.*;
/**
 * 从 JEI 运行时收集「配方类型 id → 分类标题」，供界面显示本地化的配方类型名。
 * <p>JEI 是软依赖：本类只在 {@link #get} 里确认 JEI 已加载后才会走到引用 JEI 类的 {@link #build()}，
 * 未安装时整个类不会触发 JEI 类加载，调用方拿到 null 后自行回退。</p>
 */
public final class CCGJeiTitles {
	private static final Map<String, Component> TITLES = new HashMap<>();
	private static Boolean jeiLoaded;
	private static boolean built;
	/** 按配方类型 id 取 JEI 分类标题；JEI 未安装或查不到时返回 null */
	public static Component get(String id) {
		if (jeiLoaded == null) jeiLoaded = ModList.get().isLoaded("jei");
		if (!jeiLoaded) return null;
		build();
		return TITLES.get(id);
	}
	/**
	 * JEI 分类的 uid 与 Minecraft 配方类型的注册名不一定相同（如 Create 的 {@code create:cutting}
	 * 在 JEI 里叫 {@code create:sawing}），且没有按 MC 配方类型查询的 API，所以先按 uid 登记一遍，
	 * 再采样每个分类的第一个配方、按其 Minecraft 配方类型补登记。两轮是为了让 uid 直接命中的优先，
	 * 不被采样误覆盖（Create 的 {@code automatic_brewing} 分类里也有 mixing 配方）。
	 */
	private static void build() {
		if (built) return;
		IJeiRuntime runtime = CreateJEI.runtime;
		if (runtime == null) return;
		built = true;
		IRecipeManager manager = runtime.getRecipeManager();
		List<IRecipeCategory<?>> categories = manager.createRecipeCategoryLookup().includeHidden().get().toList();
		for (IRecipeCategory<?> category : categories)
			TITLES.putIfAbsent(category.getRecipeType().getUid().toString(), category.getTitle());
		for (IRecipeCategory<?> category : categories) {
			var type = category.getRecipeType();
			manager.createRecipeLookup(type).get().findFirst().ifPresent(recipe -> {
				if (!(recipe instanceof RecipeHolder<?> holder)) return;
				ResourceLocation id = BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType());
				if (id != null) TITLES.putIfAbsent(id.toString(), category.getTitle());
			});
		}
	}
}
