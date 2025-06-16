package com.forgestove.create_cyber_goggles.mixin.other;
import com.forgestove.create_cyber_goggles.CCG;
import com.simibubi.create.compat.jei.CreateJEI;
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
	private int loadCategories(int height) {
		return CCG.CONFIG.other.nonrandomScrap ? height + 40 : height;
	}
}
