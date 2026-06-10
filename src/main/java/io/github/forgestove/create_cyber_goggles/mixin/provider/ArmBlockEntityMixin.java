package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.outliner;
@Mixin(ArmBlockEntity.class)
public abstract class ArmBlockEntityMixin implements ItemRenderable, OutlineRenderable, Self<ArmBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		return thiz().heldItem;
	}
	@Override
	public void ccg$render() {
		List.of(thiz().inputs, thiz().outputs).forEach(points -> {
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
