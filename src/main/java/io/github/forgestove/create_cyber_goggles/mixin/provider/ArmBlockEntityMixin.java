package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.*;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.outliner;
@Mixin(value = ArmBlockEntity.class, remap = false)
public abstract class ArmBlockEntityMixin extends KineticBlockEntity implements IItemRenderable, IOutlineRenderable {
	@Shadow ItemStack heldItem;
	@Shadow List<ArmInteractionPoint> inputs;
	@Shadow List<ArmInteractionPoint> outputs;
	public ArmBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Override
	public ItemStack ccg$getItemStack() {
		return heldItem;
	}
	@Override
	public void ccg$render() {
		var allPoints = new ArrayList<ArmInteractionPoint>();
		allPoints.addAll(inputs);
		allPoints.addAll(outputs);
		for (var point : allPoints) {
			if (!point.isValid()) continue;
			var level = point.getLevel();
			var pos = point.getPos();
			outliner.chaseAABB("ArmIOBox" + point, level.getBlockState(pos).getShape(level, pos).bounds().move(pos))
				.withFaceTextures(AllSpecialTextures.HIGHLIGHT_CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(point.getMode().getColor());
			outliner.showLine("ArmIOLine" + point, getBlockPos().getCenter(), point.getPos().getCenter())
				.lineWidth(1 / 8f)
				.colored(point.getMode().getColor());
		}
	}
}
