package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BlazeBurnerBlockEntity.class)
public abstract class BlazeBurnerBlockEntityMixin implements Self<BlazeBurnerBlockEntity> {
	@Shadow protected int remainingBurnTime;
	@Inject(method = "tick", at = @At("HEAD"))
	public void tick(CallbackInfo callbackInfo) {
		if (!CCG.config.goggles.enhancedInfo) return;
		var bbbe = thiz();
		var level = bbbe.getLevel();
		if (level == null || !level.isClientSide()) return;
		if (bbbe.isCreative) return;
		if (remainingBurnTime > 0) remainingBurnTime--;
	}
}
