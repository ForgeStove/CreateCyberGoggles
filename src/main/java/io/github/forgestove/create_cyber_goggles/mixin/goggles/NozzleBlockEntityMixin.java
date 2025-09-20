package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.content.kinetics.fan.NozzleBlockEntity;
import io.github.forgestove.create_cyber_goggles.*;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;

import java.util.List;
@Mixin(value = NozzleBlockEntity.class, remap = false)
public abstract class NozzleBlockEntityMixin implements IHaveGoggleInformation {
	@Shadow private boolean pushing;
	@Shadow private float range;
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return false;
		return Common.addFanTooltip(tooltip, pushing, range, 2);
	}
}
