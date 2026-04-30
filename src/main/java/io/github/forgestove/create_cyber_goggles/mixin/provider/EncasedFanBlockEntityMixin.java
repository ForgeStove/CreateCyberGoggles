package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import io.github.forgestove.create_cyber_goggles.core.event.Outliner;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
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
	implements IHaveGoggleInformation, OutlineRenderable, Self<EncasedFanBlockEntity> {
	public EncasedFanBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		var airCurrent = thiz().getAirCurrent();
		var thiz = GoggleTooltipUtil.fan(tooltip, airCurrent.pushing, airCurrent.maxDistance);
		var sup = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		return thiz || sup;
	}
	@Override
	public void ccg$render() {
		var airCurrent = thiz().getAirCurrent();
		var color = Outliner.getColor(airCurrent.pushing);
		var bounds = airCurrent.bounds;
		outliner.showAABB("FanAirBox" + this, bounds)
			.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.lineWidth(1 / 16f)
			.colored(color);
		var numberOfFlowBoxes = Outliner.getNumberOfFlowBoxes(airCurrent.maxDistance);
		var useMinSide = airCurrent.pushing == airCurrent.direction.getAxisDirection().getStep() > 0;
		var axis = airCurrent.direction.getAxis();
		var axisMin = switch (axis) {
			case X -> bounds.minX;
			case Y -> bounds.minY;
			case Z -> bounds.minZ;
		};
		var axisMax = switch (axis) {
			case X -> bounds.maxX;
			case Y -> bounds.maxY;
			case Z -> bounds.maxZ;
		};
		for (var i = 0; i < numberOfFlowBoxes; i++) {
			var offsetScale = Outliner.getOffsetScale(i, numberOfFlowBoxes);
			var id = "FanAirFlowBox" + this + i;
			if (offsetScale > 0.98) {
				outliner.remove(id);
				continue;
			}
			var offsetDistance = airCurrent.maxDistance * offsetScale;
			var pos = useMinSide ? axisMin + offsetDistance : axisMax - offsetDistance;
			var flowBound = switch (axis) {
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
