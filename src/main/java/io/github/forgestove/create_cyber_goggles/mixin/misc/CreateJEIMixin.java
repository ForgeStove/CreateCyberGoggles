package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.compat.jei.CreateJEI;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(CreateJEI.class)
public abstract class CreateJEIMixin {
	@ModifyArg(
		method = "loadCategories", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/compat/jei/category/CreateRecipeCategory$Builder;emptyBackground(II)"
			+ "Lcom/simibubi/create/compat/jei/category/CreateRecipeCategory$Builder;"
	), index = 1
	)
	public int loadCategories(int height) {
		return CCG.CONFIG.misc.showScrapContent ? height + 40 : height;
	}
}
