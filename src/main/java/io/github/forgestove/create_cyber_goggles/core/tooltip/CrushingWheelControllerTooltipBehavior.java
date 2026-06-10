package io.github.forgestove.create_cyber_goggles.core.tooltip;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import net.minecraft.network.chat.Component;

import java.util.List;
public class CrushingWheelControllerTooltipBehavior extends TooltipBehaviour<CrushingWheelControllerBlockEntity>
	implements IHaveGoggleInformation {
	public CrushingWheelControllerTooltipBehavior(CrushingWheelControllerBlockEntity be) {
		super(be);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return GoggleTooltipUtil.crushingController(tooltip, blockEntity);
	}
}
