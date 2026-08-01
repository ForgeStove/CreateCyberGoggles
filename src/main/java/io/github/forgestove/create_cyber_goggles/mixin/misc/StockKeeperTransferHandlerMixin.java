package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.compat.jei.StockKeeperTransferHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(StockKeeperTransferHandler.class)
public abstract class StockKeeperTransferHandlerMixin {
	@ModifyConstant(method = "transferRecipeOnClient", constant = @Constant(intValue = 9))
	private int transferRecipeOnClient(int constant) {
		return CCG.config.misc.allowLargeCrafting ? 81 : constant;
	}
}
