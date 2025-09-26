package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.fan.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.OutlineRenderer;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.*;

import java.util.List;
@Mixin(EncasedFanBlockEntity.class)
public abstract class EncasedFanBlockEntityMixin extends KineticBlockEntity implements IHaveGoggleInformation, IOutlineRenderable {
	@Shadow public AirCurrent airCurrent;
	public EncasedFanBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		var add = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		if (!CCG.CONFIG.goggles.enhancedInfo || getSpeed() == 0) return add;
		return TooltipUtil.addFanTooltip(tooltip, airCurrent.pushing, airCurrent.maxDistance, 1);
	}
	@Override
	public void ccg$render() {
		var color = OutlineRenderer.getColor(airCurrent.pushing);
		var bounds = airCurrent.bounds;
		Outliner.getInstance()
			.chaseAABB("FanAirBox" + this, bounds)
			.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.lineWidth(1 / 16f)
			.colored(color);
		var numberOfFlowBoxes = OutlineRenderer.getNumberOfFlowBoxes(airCurrent.maxDistance);
		for (var i = 0; i < numberOfFlowBoxes; i++) {
			var offset = OutlineRenderer.getOffset(i, numberOfFlowBoxes);
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
				Outliner.getInstance().remove(id);
				continue;
			}
			Outliner.getInstance()
				.chaseAABB(id, flowBound)
				.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(color);
		}
	}
}
