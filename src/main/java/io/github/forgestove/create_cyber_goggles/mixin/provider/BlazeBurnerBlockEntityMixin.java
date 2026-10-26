package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.GoggleTooltip;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(BlazeBurnerBlockEntity.class)
public abstract class BlazeBurnerBlockEntityMixin implements IHaveGoggleInformation, Self<BlazeBurnerBlockEntity> {
	@Shadow protected int remainingBurnTime;
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return GoggleTooltip.dispatch(tooltip, thiz(), isPlayerSneaking, null);
	}
	@Inject(method = "tick", at = @At("HEAD"))
	public void tick(CallbackInfo ci) {
		if (!CCG.config.goggles.enhancedInfo) return;
		BlazeBurnerBlockEntity thiz = thiz();
		var level = thiz.getLevel();
		if (level == null || !level.isClientSide) return;
		if (thiz.isCreative) return;
		if (thiz.getRemainingBurnTime() > 0) remainingBurnTime--;
	}
}
