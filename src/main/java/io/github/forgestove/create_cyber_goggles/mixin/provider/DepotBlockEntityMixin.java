package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.*;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
@Mixin(DepotBlockEntity.class)
public abstract class DepotBlockEntityMixin implements IHaveGoggleInformation, ItemRenderable, Self<DepotBlockEntity> {
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return GoggleTooltip.dispatch(thiz(), tooltip, isPlayerSneaking, null);
	}
	@Override
	public ItemStack ccg$getItemStack() {
		if (!CCG.config.tooltip.depot) return null;
		return thiz().getHeldItem();
	}
}
