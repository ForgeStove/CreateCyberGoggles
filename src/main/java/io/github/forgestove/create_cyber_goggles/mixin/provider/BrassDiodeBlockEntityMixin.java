package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.redstone.diodes.BrassDiodeBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import io.github.forgestove.create_cyber_goggles.api.GoggleTooltip;
import io.github.forgestove.create_cyber_goggles.core.util.contract.*;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;

import java.util.List;
@Mixin(BrassDiodeBlockEntity.class)
public abstract class BrassDiodeBlockEntityMixin implements IHaveGoggleInformation, PulseTooltipData, Self<BrassDiodeBlockEntity> {
	@Shadow protected int state;
	@Shadow ScrollValueBehaviour maxState;
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return GoggleTooltip.dispatch(thiz(), tooltip, isPlayerSneaking, null);
	}
	@Override
	public int ccg$getState() {
		return state;
	}
	@Override
	public int ccg$getMaxState() {
		return maxState.getValue();
	}
}
