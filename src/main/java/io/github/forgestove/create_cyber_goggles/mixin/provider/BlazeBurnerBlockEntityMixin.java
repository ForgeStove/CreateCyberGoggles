package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.TooltipUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(value = BlazeBurnerBlockEntity.class, remap = false)
public abstract class BlazeBurnerBlockEntityMixin extends SmartBlockEntity implements IHaveGoggleInformation {
	@Shadow public boolean isCreative;
	@Shadow protected int remainingBurnTime;
	@Shadow protected FuelType activeFuel;
	public BlazeBurnerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return false;
		return TooltipUtil.burner(tooltip, remainingBurnTime, isCreative, activeFuel);
	}
	@Inject(method = "tick", at = @At("HEAD"))
	public void tick(CallbackInfo callbackInfo) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		if (level == null || !level.isClientSide) return;
		if (isCreative) return;
		if (remainingBurnTime > 0) remainingBurnTime--;
	}
}
