package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value = BlazeBurnerBlockEntity.class, remap = false)
public abstract class BlazeBurnerBlockEntityMixin extends SmartBlockEntity {
	@Shadow public boolean isCreative;
	@Shadow protected int remainingBurnTime;
	public BlazeBurnerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	@Inject(method = "tick", at = @At("HEAD"))
	public void tick(CallbackInfo callbackInfo) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		if (level == null || !level.isClientSide()) return;
		if (isCreative) return;
		if (remainingBurnTime > 0) remainingBurnTime--;
	}
}
