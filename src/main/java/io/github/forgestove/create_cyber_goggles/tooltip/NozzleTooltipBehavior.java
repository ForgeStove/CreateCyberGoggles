package io.github.forgestove.create_cyber_goggles.tooltip;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.content.kinetics.fan.NozzleBlockEntity;
import io.github.forgestove.create_cyber_goggles.*;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.NozzleBlockEntityAccessor;
import net.minecraft.network.chat.Component;

import java.util.List;
public class NozzleTooltipBehavior extends TooltipBehaviour<NozzleBlockEntity> implements IHaveGoggleInformation {
	public NozzleTooltipBehavior(NozzleBlockEntity be) {
		super(be);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return false;
		var accessor = (NozzleBlockEntityAccessor) blockEntity;
		return Common.addFanTooltip(tooltip, accessor.getPushing(), accessor.getRange(), 2);
	}
}
