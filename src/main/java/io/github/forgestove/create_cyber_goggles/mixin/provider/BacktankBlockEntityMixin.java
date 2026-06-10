package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.equipment.armor.BacktankBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import io.github.forgestove.create_cyber_goggles.core.util.BacktankBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BacktankBlockEntity.class)
public abstract class BacktankBlockEntityMixin extends KineticBlockEntity
	implements BacktankBlockEntityAccessor, Self<BacktankBlockEntity> {
	@Unique public int ccg$leftTick;
	@Unique public int ccg$prevAirLevel;
	@Shadow public int airLevel;
	@Shadow public int airLevelTimer;
	@Shadow private int capacityEnchantLevel;
	public BacktankBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Inject(method = "tick", at = @At(value = "RETURN", ordinal = 3))
	public void tick(CallbackInfo ci, @Local(name = "max") int max) {
		if (!CCG.config.goggles.enhancedInfo) return;
		if (airLevel == max) return;
		ccg$prevAirLevel = airLevel;
		var abs = Math.abs(getSpeed());
		var increment = Mth.clamp(((int) abs - 100) / 20, 1, 5);
		airLevel = Math.min(max, airLevel + increment);
		airLevelTimer = Mth.clamp((int) (128f - abs / 5f) - 108, 0, 20);
		ccg$leftTick = (max - airLevel) / increment * Math.max(1, airLevelTimer);
	}
	@Override
	public int ccg$getLeftTick() {
		return ccg$leftTick;
	}
	@Override
	public int ccg$getCapacityEnchantLevel() {
		return capacityEnchantLevel;
	}
	@ModifyVariable(method = "read", at = @At("STORE"), name = "prev")
	private int modifyPrev(int prev) {
		return ccg$prevAirLevel;
	}
}
