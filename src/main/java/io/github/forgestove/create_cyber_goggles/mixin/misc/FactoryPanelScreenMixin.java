package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(FactoryPanelScreen.class)
public abstract class FactoryPanelScreenMixin {
	@ModifyConstant(method = "mouseScrolled", constant = @Constant(intValue = 64))
	public int modifyMaxScrollAmount(int original) {
		return CCG.config.misc.removeRequestLimit ? Integer.MAX_VALUE : original;
	}
	@ModifyConstant(method = "mouseScrolled", constant = @Constant(intValue = 10))
	public int modifyPerScrollAmount(int original, @Local(name = "itemStack") BigItemStack itemStack) {
		return CCG.config.misc.removeRequestLimit ? Item.DEFAULT_MAX_STACK_SIZE - (itemStack.count == 1 ? 1 : 0) : original;
	}
}
