package io.github.forgestove.create_cyber_goggles.core.tooltip;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import com.zurrtum.create.content.kinetics.fan.EncasedFanBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.TooltipUtil;
import net.minecraft.network.chat.Component;

import java.util.List;

public class EncasedFanTooltipBehavior<T extends EncasedFanBlockEntity> extends KineticTooltipBehaviour<T>
	implements IHaveGoggleInformation {
	public EncasedFanTooltipBehavior(T be) {
		super(be);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		var add = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		if (!CCG.CONFIG.goggles.enhancedInfo || blockEntity.getSpeed() == 0) return add;
		var airCurrent = blockEntity.getAirCurrent();
		if (airCurrent == null) return add;
		return TooltipUtil.fan(tooltip, airCurrent.pushing, airCurrent.maxDistance, 1);
	}
}
