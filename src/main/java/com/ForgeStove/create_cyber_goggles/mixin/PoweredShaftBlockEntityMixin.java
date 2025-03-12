package com.ForgeStove.create_cyber_goggles.mixin;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(PoweredShaftBlockEntity.class) public abstract class PoweredShaftBlockEntityMixin
		extends GeneratingKineticBlockEntity {
	public PoweredShaftBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
	private void addToGoggleTooltip(
			List<Component> tooltip,
			boolean isPlayerSneaking,
			CallbackInfoReturnable<Boolean> returnable
	) {
		returnable.setReturnValue(true);
		super.addToGoggleTooltip(tooltip, isPlayerSneaking);
	}
}
