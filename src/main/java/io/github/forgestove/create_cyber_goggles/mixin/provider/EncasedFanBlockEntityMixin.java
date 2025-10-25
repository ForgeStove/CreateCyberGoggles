package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.Outliner;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.outliner;
@Mixin(EncasedFanBlockEntity.class)
public abstract class EncasedFanBlockEntityMixin extends KineticBlockEntity
	implements IHaveGoggleInformation, IOutlineRenderable, ISelf<EncasedFanBlockEntity> {
	public EncasedFanBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		var airCurrent = self().getAirCurrent();
		var fan = TooltipUtil.fan(tooltip, airCurrent.pushing, airCurrent.maxDistance, 1);
		var add = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		if (!CCG.CONFIG.goggles.enhancedInfo || getSpeed() == 0) return add;
		return fan;
	}
	@Override
	public void ccg$render() {
		var airCurrent = self().getAirCurrent();
		var color = Outliner.getColor(airCurrent.pushing);
		var bounds = airCurrent.bounds;
		outliner.showAABB("FanAirBox" + this, bounds)
			.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.lineWidth(1 / 16f)
			.colored(color);
		var numberOfFlowBoxes = Outliner.getNumberOfFlowBoxes(airCurrent.maxDistance);
		for (var i = 0; i < numberOfFlowBoxes; i++) {
			var offset = Outliner.getOffset(i, numberOfFlowBoxes);
			var offsetDistance = airCurrent.maxDistance * offset;
			var axis = airCurrent.direction.getAxis();
			var min = switch (axis) {
				case X -> bounds.minX;
				case Y -> bounds.minY;
				case Z -> bounds.minZ;
			};
			var max = switch (axis) {
				case X -> bounds.maxX;
				case Y -> bounds.maxY;
				case Z -> bounds.maxZ;
			};
			var pos = airCurrent.pushing == airCurrent.direction.getAxisDirection().getStep() > 0
				? min + offsetDistance
				: max - offsetDistance;
			var flowBound = switch (axis) {
				case X -> new AABB(pos, bounds.minY, bounds.minZ, pos, bounds.maxY, bounds.maxZ);
				case Y -> new AABB(bounds.minX, pos, bounds.minZ, bounds.maxX, pos, bounds.maxZ);
				case Z -> new AABB(bounds.minX, bounds.minY, pos, bounds.maxX, bounds.maxY, pos);
			};
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
