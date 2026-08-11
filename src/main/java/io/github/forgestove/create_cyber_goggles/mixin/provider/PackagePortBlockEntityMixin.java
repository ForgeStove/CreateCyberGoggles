package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.zurrtum.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.Mixin;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.outliner;
@Mixin(PackagePortBlockEntity.class)
public abstract class PackagePortBlockEntityMixin extends SmartBlockEntity implements OutlineRenderable, Self<PackagePortBlockEntity> {
	public PackagePortBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	@Override
	public void ccg$render() {
		var ppbe = thiz();
		var target = ppbe.target;
		if (target == null) return;
		var pos = ppbe.getBlockPos();
		if (level == null) return;
		var be = target.be(level, pos);
		if (be == null) return;
		var bePos = be.getBlockPos();
		var source = Vec3.atBottomCenterOf(pos);
		var exactTarget = target.getExactTargetLocation(ppbe, level, pos);
		if (exactTarget == Vec3.ZERO) return;
		if (be instanceof ChainConveyorBlockEntity && exactTarget.closerThan(Vec3.atCenterOf(bePos), 1))
			exactTarget = exactTarget.add(0, -0.25, 0);
		var color = 0x9EDE73;
		outliner.showLine("PackagePortConnection" + this, source, exactTarget).lineWidth(1 / 8f).colored(color);
		outliner.showAABB("ChainPointSelected" + this, new AABB(exactTarget, exactTarget))
			.colored(color)
			.lineWidth(1 / 5f)
			.disableLineNormals();
	}
}
