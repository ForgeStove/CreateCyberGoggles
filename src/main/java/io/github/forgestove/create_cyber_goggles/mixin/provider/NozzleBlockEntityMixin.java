package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.fan.NozzleBlockEntity;
import io.github.forgestove.create_cyber_goggles.api.*;
import io.github.forgestove.create_cyber_goggles.event.Outliner;
import io.github.forgestove.create_cyber_goggles.util.contract.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.*;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.util.CCGUtil.outliner;
@Mixin(NozzleBlockEntity.class)
public abstract class NozzleBlockEntityMixin
	implements IHaveGoggleInformation, OutlineRenderable, NozzleTooltipData, Self<NozzleBlockEntity> {
	@Shadow private boolean pushing;
	@Shadow private float range;
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return GoggleTooltip.dispatch(tooltip, thiz(), isPlayerSneaking, null);
	}
	@Override
	public boolean ccg$getPushing() {
		return pushing;
	}
	@Override
	public float ccg$getRange() {
		return range;
	}
	@Override
	public void ccg$render() {
		Vec3 center = thiz().getBlockPos().getCenter();
		int color = Outliner.getColor(pushing);
		float range = this.range / 2F;
		outliner.chaseAABB("NozzleAirBox" + this, new AABB(center, center).inflate(range))
			.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.lineWidth(1 / 16f)
			.colored(color);
		int numberOfFlowBoxes = Outliner.getNumberOfFlowBoxes(this.range);
		for (var i = 0; i < numberOfFlowBoxes; i++) {
			double offsetScale = Outliner.getOffsetScale(i, numberOfFlowBoxes);
			String id = "NozzleAirFlowBox" + this + i;
			if (offsetScale > 0.98) {
				outliner.remove(id);
				continue;
			}
			double radius = (pushing ? offsetScale : 1 - offsetScale) * range;
			AABB flowBound = new AABB(center, center).inflate(radius);
			outliner.chaseAABB(id, flowBound)
				.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(color);
		}
	}
}
