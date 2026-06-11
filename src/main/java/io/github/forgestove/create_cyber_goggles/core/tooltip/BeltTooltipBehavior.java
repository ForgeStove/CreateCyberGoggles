package io.github.forgestove.create_cyber_goggles.core.tooltip;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;
public class BeltTooltipBehavior extends KineticTooltipBehaviour<BeltBlockEntity> {
	public BeltTooltipBehavior(BeltBlockEntity be) {
		super(be);
	}
	@Override
	public boolean addToGoggleTooltip(@NonNull List<Component> tooltip, boolean isPlayerSneaking) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		if (!(blockEntity instanceof Rate rate)) return false;
		var sup = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		GoggleTooltipUtil.beltThroughput(tooltip, rate.ccg$getRate());
		return sup;
	}
}
