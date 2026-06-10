package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.content.kinetics.fan.NozzleBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import io.github.forgestove.create_cyber_goggles.core.event.Outliner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.outliner;
@Mixin(NozzleBlockEntity.class)
public abstract class NozzleBlockEntityMixin extends SmartBlockEntity implements OutlineRenderable, Self<NozzleBlockEntity> {
	@Shadow private boolean pushing;
	@Shadow private float range;
	public NozzleBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	@Override
	public void ccg$render() {
		var center = thiz().getBlockPos().getCenter();
		var color = Outliner.getColor(pushing);
		var range = this.range / 2F;
		outliner.chaseAABB("NozzleAirBox" + this, new AABB(center, center).inflate(range))
			.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.lineWidth(1 / 16f)
			.colored(color);
		var numberOfFlowBoxes = Outliner.getNumberOfFlowBoxes(this.range);
		for (var i = 0; i < numberOfFlowBoxes; i++) {
			var offsetScale = Outliner.getOffsetScale(i, numberOfFlowBoxes);
			var id = "NozzleAirFlowBox" + this + i;
			if (offsetScale > 0.98) {
				outliner.remove(id);
				continue;
			}
			var radius = (pushing ? offsetScale : 1 - offsetScale) * range;
			var flowBound = new AABB(center, center).inflate(radius);
			outliner.chaseAABB(id, flowBound)
				.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(color);
		}
	}
}
