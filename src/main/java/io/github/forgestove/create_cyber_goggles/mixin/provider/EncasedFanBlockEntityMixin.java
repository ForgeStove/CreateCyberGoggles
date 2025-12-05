package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.fan.EncasedFanBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.event.Outliner;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.outliner;
@Mixin(EncasedFanBlockEntity.class)
public abstract class EncasedFanBlockEntityMixin extends KineticBlockEntity
	implements IHaveGoggleInformation, OutlineRenderable, Self<EncasedFanBlockEntity> {
	public EncasedFanBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		var airCurrent = self().getAirCurrent();
		if (airCurrent == null) return false;
		return GoggleTooltipUtil.fan(tooltip, airCurrent.pushing, airCurrent.maxDistance);
	}
	@Override
	public void ccg$render() {
		var airCurrent = self().getAirCurrent();
		if (airCurrent == null) return;
		var color = Outliner.getColor(airCurrent.pushing);
		var bounds = airCurrent.bounds;
		outliner.showAABB("FanAirBox" + this, bounds)
			.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.lineWidth(1 / 16f)
			.colored(color);
		var numberOfFlowBoxes = Outliner.getNumberOfFlowBoxes(airCurrent.maxDistance);
		for (var i = 0; i < numberOfFlowBoxes; i++) {
			var offset = Outliner.getOffset(i, numberOfFlowBoxes);
			var flowBound = AirCurrentUtil.calculateFlowBound(airCurrent, bounds, offset);
			var id = "FanAirFlowBox" + this + i;
			if (offset > 0.98) {
				outliner.remove(id);
				continue;
			}
			outliner.chaseAABB(id, flowBound)
				.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(color);
		}
	}
}
