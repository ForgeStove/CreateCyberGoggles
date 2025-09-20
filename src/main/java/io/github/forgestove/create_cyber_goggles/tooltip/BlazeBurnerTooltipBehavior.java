package io.github.forgestove.create_cyber_goggles.tooltip;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity;
import io.github.forgestove.create_cyber_goggles.*;
import net.minecraft.network.chat.Component;

import java.util.List;
public class BlazeBurnerTooltipBehavior extends TooltipBehaviour<BlazeBurnerBlockEntity> implements IHaveGoggleInformation {
	public BlazeBurnerTooltipBehavior(BlazeBurnerBlockEntity be) {
		super(be);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return false;
		return Common.addBurnerTooltip(tooltip, blockEntity.getRemainingBurnTime(), blockEntity.isCreative, blockEntity.getActiveFuel());
	}
}
