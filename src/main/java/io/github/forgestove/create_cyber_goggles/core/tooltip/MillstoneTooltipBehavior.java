package io.github.forgestove.create_cyber_goggles.core.tooltip;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import com.zurrtum.create.content.kinetics.millstone.MillstoneBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;
public class MillstoneTooltipBehavior extends KineticTooltipBehaviour<MillstoneBlockEntity> implements IHaveGoggleInformation {
	public MillstoneTooltipBehavior(MillstoneBlockEntity be) {
		super(be);
	}
	@Override
	public boolean addToGoggleTooltip(@NonNull List<Component> tooltip, boolean isPlayerSneaking) {
		var sup = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		var thiz = GoggleTooltipUtil.millstone(tooltip, blockEntity);
		return thiz || sup;
	}
}
