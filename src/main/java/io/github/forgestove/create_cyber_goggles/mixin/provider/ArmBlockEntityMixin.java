package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.content.kinetics.mechanicalArm.*;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.minecraft.world.item.ItemStack;
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
		List.of(inputs, outputs).forEach(points -> {
			var center = thiz().getBlockPos().getCenter();
			for (var point : points) {
				if (!point.isValid()) continue;
				var level = point.getLevel();
				var pos = point.getPos();
				var color = point.getMode().getColor();
				outliner.showAABB("ArmIOBox" + point, level.getBlockState(pos).getShape(level, pos).bounds().move(pos))
					.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
					.lineWidth(1 / 16f)
					.colored(color);
				outliner.showLine("ArmIOLine" + point, center, point.getPos().getCenter()).lineWidth(1 / 8f).colored(color);
			}
		});
	}
}
