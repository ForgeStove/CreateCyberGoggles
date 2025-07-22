package io.github.forgestove.create_cyber_goggles.mixin;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.simibubi.create.content.logistics.filter.*;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(AbstractFilterScreen.class)
public abstract class AbstractFilterScreenMixin<F extends AbstractFilterMenu> extends AbstractSimiContainerScreen<F> {
	public AbstractFilterScreenMixin(F container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	@WrapWithCondition(
		method = "containerTick", at = @At(
		value = "INVOKE",
		target = "Lio/github/fabricators_of_create/porting_lib/util/PlayerEntityHelper;closeScreen"
			+ "(Lnet/minecraft/world/entity/player/Player;)V"
	)
	)
	public boolean containerTick(Player instance) {
		return menu.containerId != -1;
	}
}
