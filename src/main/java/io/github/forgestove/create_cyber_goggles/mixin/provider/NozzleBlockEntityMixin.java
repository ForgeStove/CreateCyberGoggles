package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.fan.NozzleBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.event.Outliner;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.*;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.outliner;
@Mixin(NozzleBlockEntity.class)
public abstract class NozzleBlockEntityMixin implements IHaveGoggleInformation, OutlineRenderable, Self<NozzleBlockEntity> {
	@Shadow private boolean pushing;
	@Shadow private float range;
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return GoggleTooltipUtil.fan(tooltip, pushing, range / 2F);
	}
	@Override
	public void ccg$render() {
		var center = self().getBlockPos().getCenter();
		var color = Outliner.getColor(pushing);
		var range = this.range / 2F;
		outliner.chaseAABB("NozzleAirBox" + this, new AABB(center, center).inflate(range))
			.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.lineWidth(1 / 16f)
			.colored(color);
		var numberOfFlowBoxes = Outliner.getNumberOfFlowBoxes(this.range);
		for (var i = 0; i < numberOfFlowBoxes; i++) {
			var offset = Outliner.getOffset(i, numberOfFlowBoxes);
			var id = "NozzleAirFlowBox" + this + i;
			if (offset > 0.98) {
				outliner.remove(id);
				continue;
			}
			var radius = pushing ? offset * range : (1 - offset) * range;
			var flowBound = new AABB(center, center).inflate(radius);
			outliner.chaseAABB(id, flowBound)
				.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(color);
		}
	}
}
