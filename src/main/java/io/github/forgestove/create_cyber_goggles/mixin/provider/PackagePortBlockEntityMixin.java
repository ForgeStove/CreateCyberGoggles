package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.logistics.packagePort.*;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.IOutlineRenderable;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.*;
@Mixin(value = PackagePortBlockEntity.class, remap = false)
public abstract class PackagePortBlockEntityMixin extends SmartBlockEntity implements IOutlineRenderable {
	@Shadow public PackagePortTarget target;
	public PackagePortBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	@Override
	public void ccg$render() {
		var pos = getBlockPos();
		if (target == null) return;
		var source = Vec3.atBottomCenterOf(pos);
		var exactTarget = target.getExactTargetLocation((PackagePortBlockEntity) (Object) this, level, pos);
		if (exactTarget == Vec3.ZERO) return;
		var color = 0x9EDE73;
		Outliner.getInstance().showLine("PackagePortConnection" + this, source, exactTarget).lineWidth(1 / 8f).colored(color);
		Outliner.getInstance()
			.chaseAABB("ChainPointSelected" + this, new AABB(exactTarget, exactTarget))
			.colored(color)
			.lineWidth(1 / 5f)
			.disableLineNormals();
	}
}
