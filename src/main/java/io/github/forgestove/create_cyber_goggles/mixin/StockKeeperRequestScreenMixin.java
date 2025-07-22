package io.github.forgestove.create_cyber_goggles.mixin;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.simibubi.create.content.logistics.stockTicker.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperRequestScreenMixin extends AbstractContainerScreen<StockKeeperRequestMenu> {
	public StockKeeperRequestScreenMixin(StockKeeperRequestMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}
	@WrapWithCondition(
		method = "containerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;closeContainer()V")
	)
	public boolean containerTick(Player instance) {
		return menu.containerId != -1;
	}
}
