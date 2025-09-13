package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.fan.*;
import io.github.forgestove.create_cyber_goggles.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;

import java.util.List;
@Mixin(EncasedFanBlockEntity.class)
public abstract class EncasedFanBlockEntityMixin extends KineticBlockEntity implements IHaveGoggleInformation {
	public EncasedFanBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Shadow
	public abstract AirCurrent getAirCurrent();
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		if (!CCG.CONFIG.goggles.enhancedInfo) return true;
		if (getSpeed() == 0) return true;
		var airCurrent = getAirCurrent();
		Common.addFanTooltip(tooltip, airCurrent.pushing, airCurrent.maxDistance, 1);
		return true;
	}
}
