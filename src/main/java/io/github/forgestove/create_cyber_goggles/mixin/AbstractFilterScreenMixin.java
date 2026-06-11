package io.github.forgestove.create_cyber_goggles.mixin;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.zurrtum.create.client.content.logistics.filter.AbstractFilterScreen;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.content.logistics.filter.AbstractFilterMenu;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(AbstractFilterScreen.class)
public abstract class AbstractFilterScreenMixin<F extends AbstractFilterMenu> extends AbstractSimiContainerScreen<F> {
	public AbstractFilterScreenMixin(F container, Inventory inv, Component title) {
		super(container, inv, title, 0, 0);
	}
	@WrapWithCondition(
		method = "containerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;closeContainer()V")
	)
	public boolean containerTick(LocalPlayer instance) {
		return !CCG.config.misc.preventAutoCloseFilter && menu.containerId != -1;
	}
}
