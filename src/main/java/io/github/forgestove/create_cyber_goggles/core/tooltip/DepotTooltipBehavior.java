package io.github.forgestove.create_cyber_goggles.core.tooltip;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.content.logistics.depot.DepotBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;
public class DepotTooltipBehavior extends TooltipBehaviour<DepotBlockEntity> implements IHaveGoggleInformation {
	public DepotTooltipBehavior(DepotBlockEntity be) {
		super(be);
	}
	@Override
	public boolean addToGoggleTooltip(@NonNull List<Component> tooltip, boolean isPlayerSneaking) {
		if (!CCG.config.tooltip.depot) return false;
		return GoggleTooltipUtil.depot(tooltip, blockEntity.depotBehaviour.itemHandler);
	}
}
