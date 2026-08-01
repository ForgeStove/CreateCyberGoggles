package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.compat.jei.CreateJEI;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.compat.jei.RedstoneRequesterTransferHandler;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/**
 * 在 create 的 JEI 集成中追加注册红石请求器的配方转移 handler。
 */
@Mixin(CreateJEI.class)
public abstract class CreateJEIMixin {
	@Inject(method = "registerRecipeTransferHandlers", at = @At("TAIL"))
	private void registerRedstoneRequester(IRecipeTransferRegistration registration, CallbackInfo ci) {
		if (CCG.config.misc.redstoneRequesterJEIRequest)
			registration.addUniversalRecipeTransferHandler(new RedstoneRequesterTransferHandler());
	}
}
