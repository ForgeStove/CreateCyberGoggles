package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.redstone.diodes.BrassDiodeBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;

import java.util.List;
@Mixin(value = BrassDiodeBlockEntity.class, remap = false)
public abstract class BrassDiodeBlockEntityMixin implements IHaveGoggleInformation {
	@Shadow protected int state;
	@Shadow ScrollValueBehaviour maxState;
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return GoggleTooltipUtil.pulse(tooltip, state, maxState.getValue());
	}
}
