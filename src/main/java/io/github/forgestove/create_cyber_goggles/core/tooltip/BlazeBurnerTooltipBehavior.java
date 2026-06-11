package io.github.forgestove.create_cyber_goggles.core.tooltip;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;
public class BlazeBurnerTooltipBehavior extends TooltipBehaviour<BlazeBurnerBlockEntity> implements IHaveGoggleInformation {
	public BlazeBurnerTooltipBehavior(BlazeBurnerBlockEntity be) {
		super(be);
	}
	@Override
	public boolean addToGoggleTooltip(@NonNull List<Component> tooltip, boolean isPlayerSneaking) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		return GoggleTooltipUtil.burner(tooltip, blockEntity.getRemainingBurnTime(), blockEntity.isCreative, blockEntity.getActiveFuel());
	}
}
