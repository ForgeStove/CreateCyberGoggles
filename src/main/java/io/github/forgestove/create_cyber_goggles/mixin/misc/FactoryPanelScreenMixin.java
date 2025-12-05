package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(FactoryPanelScreen.class)
public abstract class FactoryPanelScreenMixin {
	@ModifyConstant(method = "mouseScrolled", constant = @Constant(intValue = 64))
	private int modifyMaxScrollAmount(int original) {
		return CCG.CONFIG.misc.removeFactoryPanelLimit ? Integer.MAX_VALUE : original;
	}
}
