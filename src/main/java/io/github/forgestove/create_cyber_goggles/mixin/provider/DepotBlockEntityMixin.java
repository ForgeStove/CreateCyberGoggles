package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.logistics.depot.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.*;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;

import java.util.List;
@Mixin(DepotBlockEntity.class)
public abstract class DepotBlockEntityMixin implements IHaveGoggleInformation, ItemRenderable, Self<DepotBlockEntity> {
	@Shadow DepotBehaviour depotBehaviour;
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return GoggleTooltipUtil.depot(tooltip, depotBehaviour.itemHandler);
	}
	@Override
	public ItemStack ccg$getItemStack() {
		if (!CCG.config.tooltip.depot) return null;
		return thiz().getHeldItem();
	}
}
