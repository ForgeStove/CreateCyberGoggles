package io.github.forgestove.create_cyber_goggles.mixin.compact;
import com.simibubi.create.AllSpecialTextures;
import com.yision.fluidlogistics.content.equipment.mechanicalFluidGun.MechanicalFluidGunBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.*;
import org.spongepowered.asm.mixin.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.outliner;
@Pseudo
@Mixin(MechanicalFluidGunBlockEntity.class)
public abstract class MechanicalFluidGunBlockEntityMixin implements OutlineRenderable, Self<MechanicalFluidGunBlockEntity> {
	@Override
	public void ccg$render() {
		var thiz = thiz();
		var level = thiz.getLevel();
		var color = CCG.config.outliner.outColor;
		var center = thiz().getBlockPos().getCenter();
		thiz.getTargets().forEach(target -> {
			var pos = target.absoluteFrom(thiz.gunPos());
			if (level == null) return;
			outliner.showAABB("FluidGunIOBox" + pos, level.getBlockState(pos).getShape(level, pos).bounds().move(pos))
				.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(color);
			outliner.showLine("FluidGunIOLine" + pos, center, pos.getCenter()).lineWidth(1 / 8f).colored(color);
		});
	}
}
