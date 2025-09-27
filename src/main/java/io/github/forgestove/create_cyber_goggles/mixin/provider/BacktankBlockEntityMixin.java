package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.equipment.armor.*;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import io.github.forgestove.create_cyber_goggles.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;
import java.util.List;
@Mixin(value = BacktankBlockEntity.class, remap = false)
public abstract class BacktankBlockEntityMixin extends KineticBlockEntity implements IHaveGoggleInformation {
	@Unique public int ccg$leftTick;
	@Unique public int ccg$prevAirLevel;
	@Shadow public int airLevel;
	@Shadow public int airLevelTimer;
	@Shadow private int capacityEnchantLevel;
	public BacktankBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		var totalBars = 20;
		var max = BacktankUtil.maxAir(capacityEnchantLevel);
		var percent = (float) airLevel / max;
		var filledBars = (int) (percent * totalBars);
		CCGLang.translate("tooltip.airLevel").forGoggles(tooltip);
		CCGLang.text(Color.HSBtoRGB(percent * 0.33f, 1.0f, 1.0f), "%d/%d".formatted(max, airLevel)).forGoggles(tooltip);
		CCGLang.text(ChatFormatting.GREEN, "|".repeat(filledBars))
			.add(CCGLang.text("|".repeat(totalBars - filledBars)).style(ChatFormatting.GRAY))
			.forGoggles(tooltip);
		if (getSpeed() == 0 || ccg$leftTick == 0) return super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		CCGLang.translate("tooltip.leftTime")
			.add(CCGLang.text(ChatFormatting.AQUA, " %.2f ".formatted(ccg$leftTick / 20f)))
			.translate("tooltip.seconds")
			.forGoggles(tooltip);
		return super.addToGoggleTooltip(tooltip, isPlayerSneaking);
	}
	@Inject(method = "tick", at = @At(value = "RETURN", ordinal = 3))
	public void tick(CallbackInfo callbackInfo, @Local(name = "max") int max) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		if (airLevel == max) return;
		ccg$prevAirLevel = airLevel;
		var abs = Math.abs(getSpeed());
		var increment = Mth.clamp(((int) abs - 100) / 20, 1, 5);
		airLevel = Math.min(max, airLevel + increment);
		airLevelTimer = Mth.clamp((int) (128f - abs / 5f) - 108, 0, 20);
		ccg$leftTick = (max - airLevel) / increment * Math.max(1, airLevelTimer);
	}
	@ModifyVariable(method = "read", at = @At("STORE"), name = "prev")
	private int modifyPrev(int prev) {
		return ccg$prevAirLevel;
	}
}
