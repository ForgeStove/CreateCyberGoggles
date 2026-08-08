package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import io.github.forgestove.create_cyber_goggles.api.OutlineRenderable;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.Mixin;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.outliner;
@Mixin(PackagePortBlockEntity.class)
public abstract class PackagePortBlockEntityMixin implements OutlineRenderable, Self<PackagePortBlockEntity> {
	@Override
	public void ccg$render() {
		var ppbe = thiz();
		var target = ppbe.target;
		if (target == null) return;
		var pos = ppbe.getBlockPos();
		var be = target.be(ppbe.getLevel(), pos);
		if (be == null) return;
		var bePos = be.getBlockPos();
		var source = Vec3.atBottomCenterOf(pos);
		var exactTarget = target.getExactTargetLocation(ppbe, ppbe.getLevel(), pos);
		if (exactTarget == Vec3.ZERO) return;
		if (be instanceof ChainConveyorBlockEntity && exactTarget.closerThan(bePos.getCenter(), 1))
			exactTarget = exactTarget.add(0, -0.25, 0);
		var color = 0x9EDE73;
		outliner.showLine("PackagePortConnection" + this, source, exactTarget).lineWidth(1 / 8f).colored(color);
		outliner.showAABB("ChainPointSelected" + this, new AABB(exactTarget, exactTarget))
			.colored(color)
			.lineWidth(1 / 5f)
			.disableLineNormals();
	}
}
