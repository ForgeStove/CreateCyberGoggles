package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.content.kinetics.fan.NozzleBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.event.Outliner;
import io.github.forgestove.create_cyber_goggles.core.util.OutlineRenderable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.outliner;
@Mixin(NozzleBlockEntity.class)
public abstract class NozzleBlockEntityMixin extends SmartBlockEntity implements OutlineRenderable {
	@Shadow private boolean pushing;
	@Shadow private float range;
	public NozzleBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	@Override
	public void ccg$render() {
		var center = getBlockPos().getCenter();
		var color = Outliner.getColor(pushing);
		outliner.chaseAABB("NozzleAirBox" + this, new AABB(center, center).inflate(range / 2f))
			.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.lineWidth(1 / 16f)
			.colored(color);
		var numberOfFlowBoxes = Outliner.getNumberOfFlowBoxes(range);
		for (var i = 0; i < numberOfFlowBoxes; i++) {
			var offset = Outliner.getOffset(i, numberOfFlowBoxes);
			var id = "NozzleAirFlowBox" + this + i;
			if (offset > 0.98) {
				outliner.remove(id);
				continue;
			}
			var radius = pushing ? offset * range / 2f : (1 - offset) * range / 2f;
			var flowBound = new AABB(center, center).inflate(radius);
			outliner.chaseAABB(id, flowBound)
				.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(color);
		}
	}
}
