package io.github.forgestove.create_cyber_goggles.core.tooltip;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import com.zurrtum.create.content.equipment.armor.BacktankBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;
public class BacktankTooltipBehavior<T extends BacktankBlockEntity> extends KineticTooltipBehaviour<T> implements IHaveGoggleInformation {
	public BacktankTooltipBehavior(T be) {
		super(be);
	}
	@Override
	public boolean addToGoggleTooltip(@NonNull List<Component> tooltip, boolean isPlayerSneaking) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		var accessor = (BacktankBlockEntityAccessor) blockEntity;
		var thiz = GoggleTooltipUtil.backtank(tooltip, blockEntity, accessor.ccg$getCapacityEnchantLevel(), accessor.ccg$getLeftTick());
		var sup = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		return sup || thiz;
	}
}
