package io.github.forgestove.create_cyber_goggles.core.tooltip;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.content.equipment.armor.BacktankBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import io.github.forgestove.create_cyber_goggles.core.util.BacktankBlockEntityAccessor;
import net.minecraft.network.chat.Component;

import java.util.List;
public class BacktankBlockEntityTooltipBehavior extends TooltipBehaviour<BacktankBlockEntity> implements IHaveGoggleInformation {
	public BacktankBlockEntityTooltipBehavior(BacktankBlockEntity be) {
		super(be);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return false;
		if (!(blockEntity instanceof BacktankBlockEntityAccessor leftTick)) return false;
		TooltipUtil.backtank(tooltip, blockEntity, leftTick.ccg$getCapacityEnchantLevel(), leftTick.ccg$getLeftTick());
		return true;
	}
}
