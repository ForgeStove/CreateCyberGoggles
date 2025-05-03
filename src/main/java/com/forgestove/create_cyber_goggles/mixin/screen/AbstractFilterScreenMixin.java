package com.forgestove.create_cyber_goggles.mixin.screen;
import com.simibubi.create.content.logistics.filter.*;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AbstractFilterScreen.class)
public abstract class AbstractFilterScreenMixin<F extends AbstractFilterMenu> extends AbstractSimiContainerScreen<F> {
	public AbstractFilterScreenMixin(F container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	@Inject(method = "containerTick", at = @At("HEAD"), remap = false, cancellable = true)
	private void containerTick(CallbackInfo callbackInfo) {
		callbackInfo.cancel();
		super.containerTick();
		handleTooltips();
		handleIndicators();
	}
	@Shadow(remap = false)
	protected abstract void handleTooltips();
	@Shadow(remap = false)
	public abstract void handleIndicators();
}
