package com.forgestove.create_cyber_goggles.mixin.jei;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.simibubi.create.compat.jei.CreateJEI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(CreateJEI.class)
public abstract class CreateJEIMixin {
	@ModifyArg(
		method = "loadCategories", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/compat/jei/CreateJEI$CategoryBuilder;emptyBackground(II)"
			+ "Lcom/simibubi/create/compat/jei/CreateJEI$CategoryBuilder;"
	), index = 1
	)
	private int loadCategories(int height) {
		if (!CCGConfig.get().jei.nonrandomScrap) return height;
		else return height + 40;
	}
}
