package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.content.redstone.diodes.BrassDiodeBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;

import java.util.List;
@Mixin(BrassDiodeBlockEntity.class)
public abstract class BrassDiodeBlockEntityMixin implements IHaveGoggleInformation {
	@Shadow public int state;
	@Shadow ServerScrollValueBehaviour maxState;
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		GoggleTooltipUtil.pulse(tooltip, state, maxState.getValue());
		return true;
	}
}
