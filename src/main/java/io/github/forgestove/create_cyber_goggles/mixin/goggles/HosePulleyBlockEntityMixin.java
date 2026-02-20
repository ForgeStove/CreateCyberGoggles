package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(HosePulleyBlockEntity.class)
public abstract class HosePulleyBlockEntityMixin extends KineticBlockEntity {
	@Shadow private boolean infinite;
	public HosePulleyBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Inject(method = "addToGoggleTooltip", at = @At("RETURN"), cancellable = true)
	public void addToGoggleTooltip(
		List<Component> tooltip,
		boolean isPlayerSneaking,
		CallbackInfoReturnable<Boolean> cir,
		@Local(name = "addToGoggleTooltip") boolean addToGoggleTooltip
	) {
		cir.setReturnValue(addToGoggleTooltip || infinite);
	}
}
