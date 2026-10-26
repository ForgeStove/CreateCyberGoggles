package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.fan.*;
import io.github.forgestove.create_cyber_goggles.api.*;
import io.github.forgestove.create_cyber_goggles.core.event.Outliner;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.core.*;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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
		return GoggleTooltip.dispatch(tooltip, thiz(), isPlayerSneaking, () -> super.addToGoggleTooltip(tooltip, isPlayerSneaking));
	}
	@Override
	public void ccg$render() {
		AirCurrent airCurrent = thiz().getAirCurrent();
		int color = Outliner.getColor(airCurrent.pushing);
		AABB bounds = airCurrent.bounds;
		outliner.showAABB("FanAirBox" + this, bounds)
			.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.lineWidth(1 / 16f)
			.colored(color);
		int numberOfFlowBoxes = Outliner.getNumberOfFlowBoxes(airCurrent.maxDistance);
		Direction direction = airCurrent.direction;
		if (direction == null) return;
		boolean useMinSide = airCurrent.pushing == direction.getAxisDirection().getStep() > 0;
		Axis axis = direction.getAxis();
		double axisMin = switch (axis) {
			case X -> bounds.minX;
			case Y -> bounds.minY;
			case Z -> bounds.minZ;
		};
		double axisMax = switch (axis) {
			case X -> bounds.maxX;
			case Y -> bounds.maxY;
			case Z -> bounds.maxZ;
		};
		for (var i = 0; i < numberOfFlowBoxes; i++) {
			double offsetScale = Outliner.getOffsetScale(i, numberOfFlowBoxes);
			String id = "FanAirFlowBox" + this + i;
			if (offsetScale > 0.98) {
				outliner.remove(id);
				continue;
			}
			double offsetDistance = airCurrent.maxDistance * offsetScale;
			double pos = useMinSide ? axisMin + offsetDistance : axisMax - offsetDistance;
			AABB flowBound = switch (axis) {
				case X -> new AABB(pos, bounds.minY, bounds.minZ, pos, bounds.maxY, bounds.maxZ);
				case Y -> new AABB(bounds.minX, pos, bounds.minZ, bounds.maxX, pos, bounds.maxZ);
				case Z -> new AABB(bounds.minX, bounds.minY, pos, bounds.maxX, bounds.maxY, pos);
			};
			outliner.chaseAABB(id, flowBound)
				.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(color);
		}
	}
}
