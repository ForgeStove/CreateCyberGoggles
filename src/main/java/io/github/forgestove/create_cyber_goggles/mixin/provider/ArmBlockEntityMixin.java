package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.content.kinetics.mechanicalArm.*;
import io.github.forgestove.create_cyber_goggles.api.*;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.outliner;
@Mixin(ArmBlockEntity.class)
public abstract class ArmBlockEntityMixin implements ItemRenderable, OutlineRenderable, Self<ArmBlockEntity> {
	@Shadow ItemStack heldItem;
	@Shadow List<ArmInteractionPoint> inputs;
	@Shadow List<ArmInteractionPoint> outputs;
	@Override
	public ItemStack ccg$getItemStack() {
		return heldItem;
	}
	@Override
	public void ccg$render() {
		Vec3 center = thiz().getBlockPos().getCenter();
		List.of(inputs, outputs).forEach(points -> {
			for (ArmInteractionPoint point : points) {
				if (!point.isValid()) continue;
				Level level = point.getLevel();
				BlockPos pos = point.getPos();
				int color = point.getMode().getColor();
				outliner.showAABB("ArmIOBox" + point, level.getBlockState(pos).getShape(level, pos).bounds().move(pos))
					.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
					.lineWidth(1 / 16f)
					.colored(color);
				outliner.showLine("ArmIOLine" + point, center, point.getPos().getCenter()).lineWidth(1 / 8f).colored(color);
			}
		});
	}
}
