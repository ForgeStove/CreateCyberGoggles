package io.github.forgestove.create_cyber_goggles.mixin.compact.fluidlogistics;
import com.simibubi.create.AllSpecialTextures;
import com.yision.fluidlogistics.content.equipment.mechanicalFluidGun.MechanicalFluidGunBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.OutlineRenderable;
import io.github.forgestove.create_cyber_goggles.util.contract.Self;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;

import static io.github.forgestove.create_cyber_goggles.util.CCGUtil.outliner;
@Pseudo
@Mixin(MechanicalFluidGunBlockEntity.class)
public abstract class MechanicalFluidGunBlockEntityMixin implements OutlineRenderable, Self<MechanicalFluidGunBlockEntity> {
	@Override
	public void ccg$render() {
		MechanicalFluidGunBlockEntity thiz = thiz();
		Level level = thiz.getLevel();
		int color = CCG.config.outliner.outColor;
		Vec3 center = thiz().getBlockPos().getCenter();
		thiz.getTargets().forEach(target -> {
			BlockPos pos = target.absoluteFrom(thiz.gunPos());
			if (level == null) return;
			outliner.showAABB("FluidGunIOBox" + pos, level.getBlockState(pos).getShape(level, pos).bounds().move(pos))
				.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(color);
			outliner.showLine("FluidGunIOLine" + pos, center, pos.getCenter()).lineWidth(1 / 8f).colored(color);
		});
	}
}
