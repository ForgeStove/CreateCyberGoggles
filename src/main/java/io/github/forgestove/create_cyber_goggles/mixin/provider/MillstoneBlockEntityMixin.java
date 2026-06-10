package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.millstone.MillstoneBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(MillstoneBlockEntity.class)
public abstract class MillstoneBlockEntityMixin extends KineticBlockEntity implements ItemRenderable, Self<MillstoneBlockEntity> {
	public MillstoneBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Override
	public ItemStack ccg$getItemStack() {
		var thiz = thiz();
		var input = thiz.capability.getItem(0);
		if (!input.isEmpty()) return input;
		for (var i = 1; i < thiz.capability.getContainerSize(); i++) {
			var stackInSlot = thiz.capability.getItem(i);
			if (!stackInSlot.isEmpty()) return stackInSlot;
		}
		return null;
	}
}
