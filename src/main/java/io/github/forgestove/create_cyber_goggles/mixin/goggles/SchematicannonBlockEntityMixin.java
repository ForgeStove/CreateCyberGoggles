package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import io.github.forgestove.create_cyber_goggles.*;
import io.github.forgestove.create_cyber_goggles.event.OverlayRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(value = SchematicannonBlockEntity.class, remap = false)
public abstract class SchematicannonBlockEntityMixin implements IHaveGoggleInformation {
	@Shadow public ItemStack missingItem;
	@Inject(method = "tickPrinter", at = @At("TAIL"))
	private void tickPrinter(CallbackInfo callbackInfo, @Local(name = "icon") ItemStack itemStack) {
		if (!CCG.CONFIG.goggles.renderExtraItems) return;
		OverlayRenderer.cannonItemStack = itemStack;
	}
	@Inject(method = "tickPrinter", at = @At(value = "RETURN", ordinal = 10))
	private void tickPrinter(CallbackInfo callbackInfo) {
		if (!CCG.CONFIG.goggles.renderExtraItems) return;
		OverlayRenderer.cannonItemStack = missingItem;
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return false;
		return TooltipHelper.addCannonTooltip(tooltip, (SchematicannonBlockEntity) (Object) this);
	}
}
