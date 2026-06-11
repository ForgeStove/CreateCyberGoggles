package io.github.forgestove.create_cyber_goggles.core.tooltip;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.content.redstone.diodes.BrassDiodeBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.BrassDiodeBlockEntityAccessor;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;
public class BrassDiodeTooltipBehavior extends TooltipBehaviour<BrassDiodeBlockEntity> implements IHaveGoggleInformation {
	public BrassDiodeTooltipBehavior(BrassDiodeBlockEntity be) {
		super(be);
	}
	@Override
	public boolean addToGoggleTooltip(@NonNull List<Component> tooltip, boolean isPlayerSneaking) {
		var accessor = (BrassDiodeBlockEntityAccessor) blockEntity;
		return GoggleTooltipUtil.pulse(tooltip, blockEntity.state, accessor.getMaxState().getValue());
	}
}
