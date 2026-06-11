package io.github.forgestove.create_cyber_goggles.core.tooltip;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import com.zurrtum.create.content.kinetics.fan.EncasedFanBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;
public class EncasedFanTooltipBehavior<T extends EncasedFanBlockEntity> extends KineticTooltipBehaviour<T> {
	public EncasedFanTooltipBehavior(T be) {
		super(be);
	}
	@Override
	public boolean addToGoggleTooltip(@NonNull List<Component> tooltip, boolean isPlayerSneaking) {
		var sup = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		if (!CCG.config.goggles.enhancedInfo || blockEntity.getSpeed() == 0) return sup;
		var airCurrent = blockEntity.getAirCurrent();
		var thiz = GoggleTooltipUtil.fan(tooltip, airCurrent.pushing, airCurrent.maxDistance);
		return sup || thiz;
	}
}
