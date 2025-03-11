package com.ForgeStove.create_project.mixin;
import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(GeneratingKineticBlockEntity.class) public abstract class GeneratingKineticBlockEntityMixin
		extends KineticBlockEntity {
	public GeneratingKineticBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
	private void addToGoggleTooltip(
			List<Component> tooltip,
			boolean isPlayerSneaking,
			CallbackInfoReturnable<Boolean> returnable
	) {
		returnable.setReturnValue(true);
		CreateLang.translate("gui.goggles.generator_stats").forGoggles(tooltip, 0);
		float stressBase = calculateAddedStressCapacity();
		CreateLang.translate("tooltip.capacityProvided").style(ChatFormatting.GRAY).forGoggles(tooltip, 0);
		float speed = getTheoreticalSpeed();
		if (speed != getGeneratedSpeed() && speed != 0) stressBase *= getGeneratedSpeed() / speed;
		float stressTotal = Math.abs(stressBase * speed);
		CreateLang.number(stressTotal)
				.translate("generic.unit.stress")
				.style(ChatFormatting.AQUA)
				.space()
				.add(CreateLang.translate("gui.goggles.at_current_speed").style(ChatFormatting.DARK_GRAY))
				.forGoggles(tooltip, 0);
		super.addToGoggleTooltip(tooltip, isPlayerSneaking);
	}
}
