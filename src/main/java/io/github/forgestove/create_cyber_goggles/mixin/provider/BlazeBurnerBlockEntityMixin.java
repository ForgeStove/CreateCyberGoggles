package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(BlazeBurnerBlockEntity.class)
public abstract class BlazeBurnerBlockEntityMixin implements IHaveGoggleInformation, Self<BlazeBurnerBlockEntity> {
	@Shadow public boolean isCreative;
	@Shadow protected int remainingBurnTime;
	@Shadow protected FuelType activeFuel;
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return GoggleTooltipUtil.burner(tooltip, remainingBurnTime, isCreative, activeFuel);
	}
	@Inject(method = "tick", at = @At("HEAD"))
	public void tick(CallbackInfo ci) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		var level = self().getLevel();
		if (level == null || !level.isClientSide) return;
		if (isCreative) return;
		if (remainingBurnTime > 0) remainingBurnTime--;
	}
}
