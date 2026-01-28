package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.content.kinetics.mechanicalArm.*;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.outliner;
@Mixin(ArmBlockEntity.class)
public abstract class ArmBlockEntityMixin implements ItemRenderable, OutlineRenderable, Self<ArmBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		return self().heldItem;
	}
	@Override
	public void ccg$render() {
		var allPoints = new ArrayList<ArmInteractionPoint>();
		allPoints.addAll(self().inputs);
		allPoints.addAll(self().outputs);
		for (var point : allPoints) {
			if (!point.isValid()) continue;
			var level = point.getLevel();
			var pos = point.getPos();
			outliner.showAABB("ArmIOBox" + point, level.getBlockState(pos).getShape(level, pos).bounds().move(pos))
				.withFaceTextures(AllSpecialTextures.HIGHLIGHT_CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(point.getMode().getColor());
			outliner.showLine("ArmIOLine" + point, self().getBlockPos().getCenter(), point.getPos().getCenter())
				.lineWidth(1 / 8f)
				.colored(point.getMode().getColor());
		}
	}
}
