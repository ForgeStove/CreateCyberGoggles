package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelScreen;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.content.logistics.BigItemStack;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(FactoryPanelScreen.class)
public abstract class FactoryPanelScreenMixin extends AbstractSimiScreen {
	@Shadow private boolean restocker;
	@Shadow private IconButton relocateButton;
	@ModifyConstant(method = "mouseScrolled", constant = @Constant(intValue = 64))
	public int modifyMaxScrollAmount(int original) {
		return CCG.config.misc.removeRequestLimit ? Integer.MAX_VALUE : original;
	}
	@ModifyConstant(method = "mouseScrolled", constant = @Constant(intValue = 10))
	public int modifyPerScrollAmount(int original, @Local(name = "itemStack") BigItemStack itemStack) {
		return CCG.config.misc.removeRequestLimit ? Item.DEFAULT_MAX_STACK_SIZE - (itemStack.count == 1 ? 1 : 0) : original;
	}
	@Inject(method = "init", at = @At("TAIL"))
	public void init(CallbackInfo ci) {
		if (!CCG.config.goggles.betterFactoryGauge) return;
		if (!restocker) return;
		relocateButton.setPosition(relocateButton.getX() - 23, relocateButton.getY() - 54);
		addRenderableWidget(relocateButton);
	}
}
