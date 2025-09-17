package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.fan.NozzleBlockEntity;
import io.github.forgestove.create_cyber_goggles.*;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;

import java.util.List;
@Mixin(value = NozzleBlockEntity.class, remap = false)
public abstract class NozzleBlockEntityMixin implements IHaveGoggleInformation {
	@Shadow public boolean pushing;
	@Shadow public float range;
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return false;
		return Common.addFanTooltip(tooltip, pushing, range, 2);
	}
}
