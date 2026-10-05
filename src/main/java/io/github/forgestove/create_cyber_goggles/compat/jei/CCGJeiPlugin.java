package io.github.forgestove.create_cyber_goggles.compat.jei;
import io.github.forgestove.create_cyber_goggles.CCG;
import mezz.jei.api.*;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.getCCGRes;
/** JEI 插件入口，用于追加注册红石请求器的配方转移 handler。*/
@JeiPlugin
@SuppressWarnings("unused")
public final class CCGJeiPlugin implements IModPlugin {
	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return getCCGRes("jei_plugin");
	}
	@Override
	public void registerRecipeTransferHandlers(@NotNull IRecipeTransferRegistration registration) {
		if (CCG.config.misc.jei.redstoneRequesterJEIRequest)
			registration.addUniversalRecipeTransferHandler(new RedstoneRequesterTransferHandler());
	}
}
