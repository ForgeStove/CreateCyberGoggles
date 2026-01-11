package io.github.forgestove.create_cyber_goggles.core.tooltip;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.content.redstone.diodes.BrassDiodeBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.BrassDiodeBlockEntityAccessor;
import net.minecraft.network.chat.Component;

import java.util.List;
public class BrassDiodeBlockEntityTooltipBehavior extends TooltipBehaviour<BrassDiodeBlockEntity> implements IHaveGoggleInformation {
	public BrassDiodeBlockEntityTooltipBehavior(BrassDiodeBlockEntity be) {
		super(be);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		var accessor = (BrassDiodeBlockEntityAccessor) blockEntity;
		GoggleTooltipUtil.pulse(tooltip, blockEntity.state, accessor.getMaxState().getValue());
		return true;
	}
}
