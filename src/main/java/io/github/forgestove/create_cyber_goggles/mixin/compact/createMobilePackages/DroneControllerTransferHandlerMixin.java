package io.github.forgestove.create_cyber_goggles.mixin.compact.createMobilePackages;
import de.theidler.create_mobile_packages.compat.jei.DroneControllerTransferHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
@Pseudo
@Mixin(DroneControllerTransferHandler.class)
public abstract class DroneControllerTransferHandlerMixin {
	@ModifyConstant(method = "transferRecipeOnClient", constant = @Constant(intValue = 9))
	private int ccg$allowLargeCrafting(int constant) {
		return CCG.config.misc.jei.allowLargeCrafting ? 81 : constant;
	}
	@ModifyConstant(method = "transferRecipeOnClient", constant = @Constant(longValue = 9L))
	private long ccg$allowLargeCraftingLong(long constant) {
		return CCG.config.misc.jei.allowLargeCrafting ? 81L : constant;
	}
	@ModifyConstant(method = "canFitNewTypes", constant = @Constant(intValue = 9))
	private int ccg$largeTypeLimit(int constant) {
		return CCG.config.misc.jei.allowLargeCrafting ? 81 : constant;
	}
}
