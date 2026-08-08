package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.ItemRenderable;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
@Mixin(CrushingWheelControllerBlockEntity.class)
public abstract class CrushingWheelControllerBlockEntityMixin
	implements IHaveGoggleInformation, ItemRenderable, Self<CrushingWheelControllerBlockEntity> {
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return GoggleTooltipUtil.crushingController(tooltip, thiz());
	}
	@Override
	public ItemStack ccg$getItemStack() {
		if (!CCG.config.tooltip.crushingController) return null;
		var thiz = thiz();
		if (thiz.processingEntity instanceof ItemEntity ie) return ie.getItem();
		var inventory = thiz.inventory;
		for (var i = 0; i < inventory.getSlots(); i++) {
			var stackInSlot = inventory.getStackInSlot(i);
			if (!stackInSlot.isEmpty()) return stackInSlot;
		}
		return null;
	}
}
